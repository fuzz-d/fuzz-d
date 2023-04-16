package fuzzd

import dafnyLexer
import dafnyParser
import fuzzd.interpreter.Interpreter
import fuzzd.recondition.visitor.DafnyVisitor
import org.antlr.v4.runtime.CharStreams
import java.io.File

class InterpreterRunner(private val dir: File, private val logger: fuzzd.logging.Logger) {
    private val interpreter = Interpreter()

    fun run(file: File) {
        logger.log { "Lexing & Parsing ${file.name}"}
        val input = file.inputStream()
        val cs = CharStreams.fromStream(input)
        val tokens = org.antlr.v4.runtime.CommonTokenStream(dafnyLexer(cs))
        val ast = DafnyVisitor().visitProgram(dafnyParser(tokens).program())
        logger.log { "Interpreting ${file.name}" }

        val output = interpreter.interpretDafny(ast)

        logger.log { "Completed interpreting ${file.name}. Got output:" }
        logger.log { output }
    }
}
