package voodoo.data.lock

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import voodoo.data.Side
import voodoo.data.curse.CurseConstancts.PROXY_URL
import voodoo.provider.Provider
import voodoo.provider.ProviderBase
import java.time.Instant

/**
 * Created by nikky on 28/03/18.
 * @author Nikky
 */

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class LockEntry(
        @JsonInclude(JsonInclude.Include.ALWAYS)
        var provider: String = "",
        @JsonInclude(JsonInclude.Include.ALWAYS)
        var name: String = "",
        var curseMetaUrl: String = PROXY_URL,
        var folder: String = "mods",
        var fileName: String? = null,
        var side: Side = Side.BOTH,
        // CURSE
        var projectID: Int = -1,
        var fileID: Int = -1,
        // DIRECT
        var url: String = "",
        var useUrlTxt: Boolean = true,
        // JENKINS
        var jenkinsUrl: String = "",
        var job: String = "",
        var buildNumber: Int = -1,
        var fileNameRegex: String = ".*(?<!-sources\\.jar)(?<!-api\\.jar)(?<!-deobf\\.jar)(?<!-lib\\.jar)(?<!-slim\\.jar)$",
        // JSON
        var updateJson: String = "",
        var jsonVersion: String = "",
        // LOCAL
        var fileSrc: String = ""
) {
        @JsonIgnore
        lateinit var parent: LockPack

        @JsonIgnore
        private fun providerBase(): ProviderBase = Provider.valueOf(provider).base

        @JsonIgnore
        fun version(): String = providerBase().getVersion(this)

        @JsonIgnore
        fun license(): String = providerBase().getLicense(this)

        @JsonIgnore
        fun thumbnail(): String = providerBase().getThumbnail(this)

        @JsonIgnore
        fun authors(): String = providerBase().getAuthors(this).joinToString(", ")

        @JsonIgnore
        fun projectPage(): String = providerBase().getProjectPage(this)

        @JsonIgnore
        fun releaseDate(): Instant? = providerBase().getReleaseDate(this)

        @JsonIgnore
        fun isCurse(): Boolean = provider == Provider.CURSE.name

        @JsonIgnore
        fun isJenkins(): Boolean = provider == Provider.JENKINS.name

        @JsonIgnore
        fun isDirect(): Boolean = provider == Provider.DIRECT.name

        @JsonIgnore
        fun isJson(): Boolean = provider == Provider.JSON.name

        @JsonIgnore
        fun sLocal(): Boolean = provider == Provider.LOCAL.name
}