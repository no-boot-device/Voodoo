package voodoo

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import mu.KLogging
import voodoo.importer.CurseImporter
import voodoo.importer.YamlImporter
import java.io.File
import kotlin.system.exitProcess

/**
 * Created by nikky on 28/03/18.
 * @author Nikky
 */

object Import : KLogging() {
    @JvmStatic
    fun main(vararg args: String) = mainBody {
//        logger.info { args.map { it } }
        val arguments = Arguments(ArgParser(args))

        arguments.run {
            logger.info { this.methode }
            val tester = when (methode) {
                "curse" -> CurseImporter
                "yaml" -> YamlImporter

                else -> {
                    logger.error("no such import methode: '$methode'")
                    exitProcess(-1)
                }
            }

            //TODO: import as ModPack and NestedPack ?

            val (nestedPack, versions) = tester.import(source = source, target = target)
//            nestedPack?.let {
//                target.writeYaml(it)
//
//                versions?.let {
//                    val flatpack = Flatten.flattenPack(nestedPack, target.absoluteFile.parentFile)
//                    flatpack.versions.putAll(it)
//                    flatpack.writeVersionCache()
//                }
//            }


            println("import successful")
        }
    }

    private class Arguments(parser: ArgParser) {
        val methode by parser.positional("METHODE",
                help = "format to import from") { this.toLowerCase() }
                .default("")

        val source by parser.positional("SOURCE",
                help = "input url/file")

        val target by parser.positional("OUTPUT",
                help = "output file") { File(this) }
    }
}