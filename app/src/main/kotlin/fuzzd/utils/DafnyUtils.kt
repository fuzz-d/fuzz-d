package fuzzd.utils

import java.io.File
import java.lang.ProcessBuilder.Redirect

const val DAFNY_ADVANCED = "advanced"
const val DAFNY_MAIN = "main"
const val DAFNY_TYPE = "dfy"
const val DAFNY_GENERATED = "generated"

fun runCommand(command: String): Process {
    return Runtime.getRuntime().exec(command)
}

fun compileDafny(targetLanguage: String, fileDir: String, fileName: String, timeout: Long): Process {
    val command =
        "timeout $timeout dafny /compileVerbose:0 /noVerify /compile:2 /spillTargetCode:1 /compileTarget:$targetLanguage $fileDir/$fileName.dfy"
    return runCommand(command)
}

fun verifyDafny(fileDir: String, fileName: String, timeout: Long): Process {
    val command = "timeout $timeout dafny /compile:0 $fileDir/$fileName.dfy"
    return runCommand(command)
}

fun Process.readInputStream(): String = String(inputStream.readAllBytes())

fun Process.readErrorStream(): String = String(errorStream.readAllBytes())
