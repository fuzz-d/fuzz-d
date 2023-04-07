package fuzzd

import fuzzd.logging.Logger
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import java.util.UUID
import kotlin.random.Random

@OptIn(ExperimentalCli::class)
class Fuzz(
    private val outputPath: String,
    private val outputDir: String,
    private val logger: Logger,
) : Subcommand("fuzz", "Generate programs to test Dafny") {
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
    private val noRun by option(
        ArgType.Boolean,
        "noRun",
        "n",
        "Generate a program without running differential testing on it",
    )

    override fun execute() {
        val generationSeed = seed?.toLong() ?: Random.Default.nextLong()
        FuzzRunner(outputPath, outputDir, logger).run(
            generationSeed,
            advanced == true,
            instrument == true,
            noRun != true,
        )
    }
}

@OptIn(ExperimentalCli::class)
class Recondition(
    private val outputPath: String,
    private val outputDir: String,
    private val logger: Logger,
) : Subcommand("recondition", "Recondition a reduced test case") {
    private val file by argument(ArgType.String, "file", "path to .dfy file to recondition")
    private val advanced by option(
        ArgType.Boolean,
        "advanced",
        "a",
        "Use advanced reconditioning to reduce use of safety wrappers",
    )

    override fun execute() = ReconditionRunner(outputPath, outputDir, logger).run(file, advanced == true)
}

@OptIn(ExperimentalCli::class)
fun createArgParser(path: String, dir: String, logger: Logger): ArgParser {
    val parser = ArgParser("fuzzd")
    val fuzz = Fuzz(path, dir, logger)
    val recondition = Recondition(path, dir, logger)
    parser.subcommands(fuzz, recondition)

    return parser
}

fun main(args: Array<String>) {
    val path = "output"
    val dir = UUID.randomUUID().toString()
    val logger = Logger(path, dir)
    val parser = createArgParser(path, dir, logger)

    try {
        parser.parse(args)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        logger.close()
    }
}
