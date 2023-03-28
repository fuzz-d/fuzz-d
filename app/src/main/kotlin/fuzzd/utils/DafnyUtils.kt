package fuzzd.utils

import java.io.File
import java.lang.ProcessBuilder.Redirect

fun runCommand(command: String): Process {
    return Runtime.getRuntime().exec(command)
}

fun runCommand(command: Array<String>): Process {
    return Runtime.getRuntime().exec(command)
}

fun runCommandRedirect(command: List<String>, outputFile: File): Process =
    ProcessBuilder().command(command).redirectOutput(Redirect.appendTo(outputFile)).start()

fun compileDafny(targetLanguage: String, fileDir: String, fileName: String): Process {
    val command =
        "dafny /compileVerbose:0 /noVerify /compile:2 /spillTargetCode:1 /compileTarget:$targetLanguage $fileDir/$fileName.dfy"
    return runCommand(command)
}

fun Process.readInputStream(): String = String(inputStream.readAllBytes())

fun Process.readErrorStream(): String = String(errorStream.readAllBytes())
