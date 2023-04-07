package fuzzd

import dafnyLexer
import dafnyParser
import fuzzd.generator.ast.DafnyAST
import fuzzd.logging.Logger
import fuzzd.logging.OutputWriter
import fuzzd.recondition.AdvancedReconditioner
import fuzzd.recondition.Reconditioner
import fuzzd.recondition.visitor.DafnyVisitor
import fuzzd.utils.ADVANCED_ABSOLUTE
import fuzzd.utils.ADVANCED_RECONDITION_CLASS
import fuzzd.utils.ADVANCED_SAFE_ARRAY_INDEX
import fuzzd.utils.ADVANCED_SAFE_DIV_INT
import fuzzd.utils.ADVANCED_SAFE_MODULO_INT
import fuzzd.utils.DAFNY_ADVANCED
import fuzzd.utils.DAFNY_BODY
import fuzzd.utils.DAFNY_TYPE
import fuzzd.utils.DAFNY_WRAPPERS
import fuzzd.utils.WRAPPER_FUNCTIONS
import fuzzd.validator.OutputValidator
import fuzzd.validator.executor.execution_handler.CsExecutionHandler
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

class ReconditionRunner(private val dir: File, private val logger: Logger) {
    private val validator = OutputValidator()

    fun run(file: File, advanced: Boolean) {
        val input = file.inputStream()
        val cs = CharStreams.fromStream(input)
        val tokens = CommonTokenStream(dafnyLexer(cs))

        val ast = DafnyVisitor().visitProgram(dafnyParser(tokens).program())
        run(ast, advanced)
    }

    fun run(ast: DafnyAST, advanced: Boolean) {
        val advancedReconditioner = AdvancedReconditioner()
        try {
            val ids = if (advanced) {
                logger.log { "Running advanced reconditioning" }

                val advancedAST = advancedReconditioner.recondition(ast)

                val writer = OutputWriter(dir, "$DAFNY_ADVANCED.$DAFNY_TYPE")
                writer.write { "$ADVANCED_RECONDITION_CLASS\n" }
                writer.write { "$ADVANCED_ABSOLUTE\n" }
                writer.write { "$ADVANCED_SAFE_ARRAY_INDEX\n" }
                writer.write { "$ADVANCED_SAFE_MODULO_INT\n" }
                writer.write { "$ADVANCED_SAFE_DIV_INT\n" }
                writer.write { advancedAST }
                writer.close()

                val output = validator.collectOutput(
                    CsExecutionHandler(
                        writer.dirPath,
                        DAFNY_ADVANCED,
                        compileTimeout = 180L,
                        executeTimeout = 60L,
                    ),
                )
                if (output != null) {
                    val safetyRegex = Regex("safety[0-9]+\\n")
                    val ids = safetyRegex.findAll(output, 0)
                        .map { it.value }
                        .map { it.substring(0, it.lastIndex) }
                        .toSet()
                    logger.log { "Advanced reconditioning gave ids: $ids " }
                    ids
                } else {
                    logger.log { "Advanced reconditioning timed out" }
                    null
                }
            } else {
                null
            }

            val reconditioner = Reconditioner(logger, ids)
            val reconditionedAST = reconditioner.recondition(ast)

            val reconditionedWriter = OutputWriter(dir, "$DAFNY_BODY.$DAFNY_TYPE")
            reconditionedWriter.write { reconditionedAST }
            reconditionedWriter.close()

            val wrappersWriter = OutputWriter(dir, "$DAFNY_WRAPPERS.$DAFNY_TYPE")
            WRAPPER_FUNCTIONS.forEach { wrapper -> wrappersWriter.write { "$wrapper\n" } }
            wrappersWriter.close()
        } catch (e: Exception) {
            logger.log { "Reconditioning threw error" }
            logger.log { "===================================" }
            logger.log { e.stackTraceToString() }
        }
    }
}
