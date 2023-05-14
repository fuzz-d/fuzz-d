package fuzzd

import dafnyLexer
import dafnyParser
import fuzzd.generator.ast.DafnyAST
import fuzzd.generator.ast.StatementAST
import fuzzd.interpreter.Interpreter
import fuzzd.logging.OutputWriter
import fuzzd.recondition.visitor.DafnyVisitor
import org.antlr.v4.runtime.CharStreams
import java.io.File

class InterpreterRunner(private val dir: File, private val logger: fuzzd.logging.Logger) {

    fun run(file: File, verify: Boolean): Pair<String, List<StatementAST>> {
        logger.log { "Lexing & Parsing ${file.name}" }
        val input = file.inputStream()
        val cs = CharStreams.fromStream(input)
        val tokens = org.antlr.v4.runtime.CommonTokenStream(dafnyLexer(cs))
        val ast = DafnyVisitor().visitProgram(dafnyParser(tokens).program())

        return run(ast, false, verify)
    }

    fun run(ast: DafnyAST, generateChecksum: Boolean, verify: Boolean): Pair<String, List<StatementAST>> {
        logger.log { "Interpreting Dafny AST" }

        val interpreter = Interpreter(generateChecksum, verify)
        val output = interpreter.interpretDafny(ast)
        val outputWriter = OutputWriter(dir, INTERPRET_FILENAME)
        outputWriter.write { output.first }
        outputWriter.close()

        logger.log { "Completed interpreting Dafny AST. Output stored in ${dir.name}/$INTERPRET_FILENAME" }

        return output
    }

    companion object {
        const val INTERPRET_FILENAME = "interpret_out.txt"
    }
}
