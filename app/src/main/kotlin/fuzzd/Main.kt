package fuzzd

import dafnyLexer
import dafnyParser
import fuzzd.generator.Generator
import fuzzd.generator.selection.SelectionManager
import fuzzd.logging.Logger
import fuzzd.logging.OutputWriter
import fuzzd.recondition.AdvancedReconditioner
import fuzzd.recondition.Reconditioner
import fuzzd.recondition.visitor.DafnyVisitor
import fuzzd.utils.ADVANCED_ABSOLUTE
import fuzzd.utils.ADVANCED_SAFE_ARRAY_INDEX
import fuzzd.utils.ADVANCED_SAFE_DIV_INT
import fuzzd.utils.ADVANCED_SAFE_MODULO_INT
import fuzzd.utils.WRAPPER_FUNCTIONS
import fuzzd.validator.OutputValidator
import fuzzd.validator.executor.execution_handler.CsExecutionHandler
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.util.UUID
import kotlin.random.Random

class Main(private val path: String) {
    private val validator = OutputValidator()

    fun fuzz(seed: Long = Random.Default.nextLong()) {
        // init
        val generator = Generator(SelectionManager(Random(seed)))
        val directory = UUID.randomUUID().toString()
        val logger = Logger(path, directory)

        logger.log { "Fuzzing with seed: $seed" }
        println("Fuzzing with seed: $seed")
        println("Output being written to directory: $directory")

        // generate program
        try {
            val ast = generator.generate()

            println("Generated ast")

            val reconditioner = Reconditioner()
            val reconditionedAST = reconditioner.recondition(ast)

            // output program
            val writer = OutputWriter(path, directory, "$DAFNY_BODY.$DAFNY_TYPE")
            writer.write { reconditionedAST }
            writer.close()

            val wrappersWriter = OutputWriter(path, directory, "$DAFNY_WRAPPERS.$DAFNY_TYPE")
            WRAPPER_FUNCTIONS.forEach { wrapper -> wrappersWriter.write { "$wrapper\n" } }
            wrappersWriter.close()

            // differential testing; log results
            val validationResult = validator.validateFile(writer.dirPath, DAFNY_WRAPPERS, DAFNY_BODY, DAFNY_MAIN)
            logger.log { validationResult }
        } catch (e: Exception) {
            // do nothing
            logger.log { "Generation threw error" }
            logger.log { "======================" }
            logger.log { e.stackTraceToString() }
        } finally {
            logger.close()
        }
    }

    companion object {
        private const val DAFNY_WRAPPERS = "wrappers"
        private const val DAFNY_MAIN = "main"
        private const val DAFNY_BODY = "body"
        private const val DAFNY_TYPE = "dfy"
    }
}

@OptIn(ExperimentalCli::class)
class Fuzz : Subcommand("fuzz", "Generate programs to test Dafny") {
    private val seed by option(ArgType.String, "seed", "s", "Generation Seed")

    override fun execute() {
        val generationSeed = seed?.toLong() ?: Random.Default.nextLong()
        Main("../output").fuzz(generationSeed)
    }
}

@OptIn(ExperimentalCli::class)
class Recondition : Subcommand("recondition", "Recondition a reduced test case") {
    private val file by argument(ArgType.String, "file",  "path to .dfy file to recondition")
    private val advanced by option(
        ArgType.Boolean,
        "advanced",
        "a",
        "Use advanced reconditioning to reduce use of safety wrappers",
    )

    override fun execute() {
        val userPath = System.getProperty("user.dir")
        val input = File("$userPath/$file").inputStream()
        val cs = CharStreams.fromStream(input)
        val tokens = CommonTokenStream(dafnyLexer(cs))

        val path = "../output"
        val dir = UUID.randomUUID().toString()

        val ast = DafnyVisitor().visitProgram(dafnyParser(tokens).program())

        val ids = if (advanced == true) {
            val advancedAST = AdvancedReconditioner().recondition(ast)

            val writer = OutputWriter(path, dir, "advanced.dfy")
            writer.write { ADVANCED_ABSOLUTE }
            writer.write { ADVANCED_SAFE_ARRAY_INDEX }
            writer.write { ADVANCED_SAFE_MODULO_INT }
            writer.write { ADVANCED_SAFE_DIV_INT }
            writer.write { advancedAST }
            writer.close()

            val output = OutputValidator().collectOutput(CsExecutionHandler(writer.dirPath, "advanced"))
            println(output)
            val necessaryIds = output?.split("\n") ?: emptyList()

            necessaryIds.subList(0, necessaryIds.size).map { it.toInt() }.toSet()
        } else {
            null
        }

        val reconditionedAST = Reconditioner(ids).recondition(ast)
        val reconditionedWriter = OutputWriter(path, dir, "reconditioned.dfy")
        reconditionedWriter.write { reconditionedAST }
        reconditionedWriter.close()
    }
}

@OptIn(ExperimentalCli::class)
fun createArgParser(): ArgParser {
    val parser = ArgParser("fuzzd")
    val fuzz = Fuzz()
    val recondition = Recondition()
    parser.subcommands(fuzz, recondition)

    return parser
}

fun main(args: Array<String>) {
    val parser = createArgParser()

    parser.parse(args)
}
