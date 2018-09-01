package voodoo

import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import voodoo.builder.resolve
import voodoo.data.flat.ModPack
import voodoo.importer.YamlImporter
import java.io.*

object BuildSpek : Spek({
    describe("import YAML") {
        val rootFolder by memoized {
            File("run").resolve("test").resolve("build").absoluteFile.apply {
                deleteRecursively()
                mkdirs()
            }
        }

        beforeEachTest {
            fileForResource("/voodoo/buildSpek").copyRecursively(rootFolder)
        }

        val mainFile by memoized {
            rootFolder.resolve("testpack.yaml")
        }
        val includeFile by memoized {
            rootFolder.resolve("include.yaml")
        }

        it("main yaml exists") {
            assert(mainFile.isFile)
        }
        it("include yaml file exists") {
            assert(includeFile.isFile)
        }

        val packFile by memoized {
            runBlocking { YamlImporter.import(source = mainFile.path, target = rootFolder, name = "lockpack") }
            rootFolder.resolve("lockpack.pack.hjson")
        }

        it("packFile exists") {
            assert(packFile.isFile) { "cannot find $packFile" }
        }

        context("loading pack") {
            val modpack by memoized {
                Builder.jankson.fromJson<ModPack>(packFile)
            }
            val entries by memoized {
                modpack.loadEntries(rootFolder, Builder.jankson)
                modpack.entrySet
            }

            it("entries are valid") {
                entries.forEach { entry ->
                    assert(entry.id.isNotBlank()) {"id of $entry is blank"}
                }
            }
            context ("building pack") {
                val lockEntries by memoized {
                    runBlocking {
                        modpack.resolve(
                                rootFolder,
                                Builder.jankson,
                                updateAll = true,
                                updateDependencies = true
                        )
                    }
                    modpack.lockEntrySet
                }

                it("validate lockentries") {
                    println("start test lockentries")
                    lockEntries.forEach { entry ->
                        println("${entry.id}: ${entry.name()}")
                        assert(entry.provider().validate(entry)) { "$entry failed validation" }
                    }
                }
            }
        }

    }
})
