package fuzzd

import fuzzd.generator.Generator
import fuzzd.generator.ast.ASTElement
import fuzzd.validator.OutputValidator
import kotlinx.coroutines.runBlocking
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.UUID
import kotlin.system.exitProcess

class Main(private val path: String) {
    private val generator = Generator()
    private val validator = OutputValidator()

    private fun writeFile(ast: ASTElement): String {
        // create directory for output files
        val directoryName = UUID.randomUUID().toString()
        val dir = File("$path/$directoryName")
        dir.mkdir()

        // create file in directory
        val mainFile = File("$path/$directoryName/$DAFNY_MAIN.$DAFNY_TYPE")
        try {
            val bw = BufferedWriter(FileWriter(mainFile.absoluteFile))
            bw.write(ast.toString())
            bw.close()
        } catch (e: IOException) {
            e.printStackTrace()
            exitProcess(-1)
        }

        return dir.absolutePath
    }

    suspend fun fuzz() {
        val ast = generator.generate()
        val dirPath = writeFile(ast)

        val validationResult = validator.validateFile(dirPath, DAFNY_MAIN)
        println("bug found: ${validationResult.erroneousResult}")
    }

    companion object {
        private const val DAFNY_MAIN = "main"
        private const val DAFNY_TYPE = "dfy"
    }
}

fun main() = runBlocking {
    Main("../output").fuzz()
}
