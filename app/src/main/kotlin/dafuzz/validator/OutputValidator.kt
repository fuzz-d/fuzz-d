package dafuzz.validator

import kotlinx.coroutines.*
import runCommand
import java.io.BufferedReader
import java.io.InputStreamReader

fun Process.readOutput(): String {
    val stdIn = BufferedReader(InputStreamReader(inputStream))
    return stdIn.readLines().joinToString("\n")
}

class OutputValidator {
    suspend fun validateFile(fileDir: String, fileName: String): String {
        val csOutput = withContext(Dispatchers.IO) {
            val process = "dafny /spillTargetCode:1 $fileDir/$fileName.dfy".runCommand()

            if (process.exitValue() == 0) {
                "dotnet $fileDir/$fileName.dll".runCommand().readOutput()
            } else {
                process.readOutput()
            }
        }

        return csOutput
    }

}
