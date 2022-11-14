package fuzzd.validator.executor

fun runCommand(command: String): Process {
    return Runtime.getRuntime().exec(command)
}

fun compileDafny(targetLanguage: String, fileDir: String, fileName: String): Process {
    val command = "dafny /compileVerbose:0 /noVerify /compile:2 /spillTargetCode:1 /compileTarget:$targetLanguage $fileDir/$fileName.dfy"
    return runCommand(command)
}

fun Process.readInputStream(): String = String(inputStream.readAllBytes())

fun Process.readErrorStream(): String = String(errorStream.readAllBytes())
