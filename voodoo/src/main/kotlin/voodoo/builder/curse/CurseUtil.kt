package voodoo.builder.curse

import aballano.kotlinmemoization.memoize
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import khttp.get
import mu.KLogging
import voodoo.builder.data.Entry
import voodoo.builder.data.Modpack
import voodoo.builder.provider.CurseProviderThing
import org.apache.commons.compress.compressors.CompressorStreamFactory
import voodoo.gen.VERSION
import java.io.*


/**
 * Created by nikky on 30/01/18.
 * @author Nikky
 * @version 1.0
 */
object CurseUtil : KLogging() {
    private val META_URL = "https://cursemeta.dries007.net" //TODO: move into Entry
    private val FEED_URL = "http://clientupdate-v6.cursecdn.com/feed/addons/432/v10" //TODO: move into Entry ?
    val useragent = "voodoo/$VERSION"

    val mapper = jacksonObjectMapper() // Enable Json parsing
            .registerModule(KotlinModule()) // Enable Kotlin support
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
//            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)

    val data: List<AddOn> = getFeed()

    fun getFeed(): List<AddOn> {
        logger.info("downloading curse feed")
        val r = get("$FEED_URL/complete.json.bz2", stream = true)
        val bis = ByteArrayInputStream(r.content)
        val input = CompressorStreamFactory().createCompressorInputStream(bis)
        val buf = BufferedReader(InputStreamReader(input))

        val text = buf.use { it.readText() }

        val feed = mapper.readValue<CurseFeed>(text)

        return feed.data.filter {
            when (it.categorySection.id) {
                6 -> true //mod
                12 -> true //texture packs
                17 -> false //worlds
                4471 -> false //modpacks
                else -> false
            }
        }
//        CurseProviderThing.logger.error("failed getting cursemeta data")
//        throw Exception("failed getting cursemeta data, code: ${r.statusCode}")
    }

    private fun getAddonFileCall(addonId: Int, fileId: Int): AddOnFile? {
        val url = "$META_URL/api/v2/direct/GetAddOnFile/$addonId/$fileId"

        logger.debug("get $url")
        val r = get(url, headers = mapOf("User-Agent" to useragent))
        if (r.statusCode == 200) {
            return mapper.readValue(r.text)
        }
        return null
    }

    val getAddonFile = ::getAddonFileCall.memoize()

    private fun GetAllFilesForAddOnCall(addonId: Int): List<AddOnFile> {
        val url = "$META_URL/api/v2/direct/GetAllFilesForAddOn/$addonId"

        logger.debug("get $url")
        val r = get(url, headers = mapOf("User-Agent" to useragent))
        if (r.statusCode == 200) {
            return mapper.readValue(r.text)
        }
        throw Exception("failed getting cursemeta data")
    }

    val GetAllFilesForAddOn = ::GetAllFilesForAddOnCall.memoize()

    private fun getAddonCall(addonId: Int): AddOn? {
        val url = "$META_URL/api/v2/direct/GetAddOn/$addonId"

        CurseProviderThing.logger.debug("get $url")
        val r = get(url, headers = mapOf("User-Agent" to useragent))
        if (r.statusCode == 200) {
            try {
                return mapper.readValue(r.text)
            } catch (e: MissingKotlinParameterException) {
                logger.error("$url $e cannot parse ${r.text}")
            }
        }
        return null
    }

    val getAddon = ::getAddonCall.memoize()


    fun findFile(entry: Entry, modpack: Modpack): Triple<Int, Int, String> {
        val mcVersions = listOf(modpack.mcVersion) + entry.validMcVersions
        val name = entry.name
        val version = entry.version
        val releaseTypes = entry.releaseTypes
//        if(releaseTypes.isEmpty()) {
//            releaseTypes = setOf(ReleaseType.RELEASE, ReleaseType.BETA) //TODO: is this not already set because i enforce defaults ?
//        }
        var addonId = entry.id
        val fileId = entry.fileId
        val fileNameRegex = entry.curseFileNameRegex

        val addon = data.find { addon ->
            (name.isNotBlank() && name.equals(addon.name, true))
                    || (addonId > 0 && addonId == addon.id)
        } ?: if(addonId > 0) getAddon(addonId)!! else {
            logger.error("no addon matching the parameters found for '$entry'")
            System.exit(-1)
            return Triple(-1, -1, "")
        }

        addonId = addon.id

        val re = Regex(fileNameRegex)

        if (fileId > 0) {
            val file = getAddonFile(addonId, fileId)
            if (file != null)
                return Triple(addonId, file.id, file.fileNameOnDisk)
        }

        var files = GetAllFilesForAddOn(addonId).sortedWith(compareByDescending { it.fileDate })

        var oldFiles = files

        if (version.isNotBlank()) {
            files = files.filter { f ->
                (f.fileName.contains(version, true) || f.fileName == version)
            }
            if (files.isEmpty()) {
                logger.error("filtered files did not match version {}", oldFiles)
            }
            oldFiles = files
        }

        if (files.isNotEmpty()) {
            files = files.filter { f ->
                mcVersions.any { v -> f.gameVersion.contains(v) }
            }

            if (files.isEmpty()) {
                logger.error("filtered files did not match mcVersion {}", oldFiles)
            }
            oldFiles = files
        }

        if (files.isNotEmpty()) {
            files = files.filter { f ->
                releaseTypes.contains(f.releaseType)
            }

            if (files.isEmpty()) {
                logger.error("filtered files did not match releaseType {}", oldFiles)
            }
            oldFiles = files
        }

        if (files.isNotEmpty()) {
            files = files.filter { f ->
                re.matches(f.fileName)
            }
            if (files.isEmpty()) {
                logger.error("filtered files did not match regex {}", oldFiles)
            }
        }

        val file = files.sortedWith(compareByDescending { it.fileDate }).firstOrNull()
        if (file == null) {
            val filesUrl = "$META_URL/api/addon/$addonId/files"
            logger.error("no matching version found for ${addon.name} addon_url: ${addon.webSiteURL} " +
                    "files: $filesUrl mc version: $mcVersions version: $version \n" +
                    "$addon")
            logger.error("no file matching the parameters found for ${addon.name}")
            System.exit(-1)
            return Triple(addonId, -1, "")
        }
        return Triple(addonId, file.id, file.fileNameOnDisk)
    }
}