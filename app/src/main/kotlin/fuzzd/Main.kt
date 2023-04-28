package fuzzd

import fuzzd.logging.Logger
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import java.io.File
import java.util.UUID
import kotlin.random.Random

@OptIn(ExperimentalCli::class)
class Fuzz : Subcommand("fuzz", "Generate programs to test Dafny") {
    private val seed by option(ArgType.String, "seed", "s", "Generation Seed")
    private val advanced by option(
        ArgType.Boolean,
        "advanced",
        "a",
        "Use advanced reconditioning to reduce use of safety wrappers",
    )
    private val instrument by option(
        ArgType.Boolean,
        "instrument",
        "i",
        "Instrument control flow with print statements for debugging program paths",
    )
    private val swarm by option(ArgType.Boolean, "swarm", "sw", "Run with swarm testing enabled")
    private val noRun by option(
        ArgType.Boolean,
        "noRun",
        "n",
        "Generate a program without running differential testing on it",
    )

    override fun execute() {
        val path = "output"
        val dir = UUID.randomUUID().toString()
        val fileDir = File("$path/$dir")
        val logger = Logger(fileDir)
        val generationSeed = seed?.toLong() ?: Random.Default.nextLong()

        try {
            FuzzRunner(fileDir, logger).run(
                generationSeed,
                advanced == true,
                instrument == true,
                noRun != true,
                swarm == true
            )
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            logger.close()
        }
    }
}

@OptIn(ExperimentalCli::class)
class Recondition : Subcommand("recondition", "Recondition a reduced test case") {
    private val file by argument(ArgType.String, "file", "path to .dfy file to recondition")
    private val advanced by option(
        ArgType.Boolean,
        "advanced",
        "a",
        "Use advanced reconditioning to reduce use of safety wrappers",
    )

    override fun execute() {
        val file = File(file)
        val logger = Logger(file.absoluteFile.parentFile, fileName = "recondition.log")
        try {
            ReconditionRunner(file.absoluteFile.parentFile, logger).run(file, advanced == true)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            logger.close()
        }
    }
}

@OptIn(ExperimentalCli::class)
class Interpret : Subcommand("interpret", "Interpret a valid .dfy file") {
    private val file by argument(ArgType.String, "file", "path to .dfy file to interpret")

    override fun execute() {
        val file = File(file)
        val logger = Logger(file.absoluteFile.parentFile, fileName = "interpret.log")
        try {
            InterpreterRunner(file.absoluteFile.parentFile, logger).run(file)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            logger.close()
        }
    }
}

@OptIn(ExperimentalCli::class)
fun createArgParser(): ArgParser {
    val parser = ArgParser("fuzzd")
    val fuzz = Fuzz()
    val recondition = Recondition()
    val interpret = Interpret()
    parser.subcommands(fuzz, recondition, interpret)

    return parser
}

fun main(args: Array<String>) {
    createArgParser().parse(args)
}
