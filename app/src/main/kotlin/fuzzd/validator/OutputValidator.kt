package fuzzd.validator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import runCommand
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

fun Process.readOutput(): String {
    val stdIn = BufferedReader(InputStreamReader(inputStream))
    return stdIn.readLines().joinToString("\n")
}

class OutputValidator {
    suspend fun validateFile(fileDir: String, fileName: String): String {
        val csOutput = withContext(Dispatchers.IO) {
            val process = "dafny /spillTargetCode:1 $fileDir/$fileName.dfy".runDiscardingOutput()

            if (process.exitValue() == 0) {
                "dotnet $fileDir/$fileName.dll".runCommand().readOutput()
            } else {
                process.readOutput()
            }
        }

        val jsOutput = withContext(Dispatchers.IO) {
            val process = "dafny /spillTargetCode:1 /compileTarget:js $fileDir/$fileName.dfy".runDiscardingOutput()

            if (process.exitValue() == 0) {
                "node $fileDir/$fileName.js".runCommand().readOutput()
            } else {
                process.readOutput()
            }
        }

        println(csOutput)
        println(jsOutput)

        return csOutput
    }

    companion object{
        val PROCESS_CONFIG: ProcessBuilder.() -> Unit = {
            redirectOutput(ProcessBuilder.Redirect.DISCARD)
        }

        fun String.runDiscardingOutput(): Process = this.runCommand(processConfig = PROCESS_CONFIG)
    }
}
