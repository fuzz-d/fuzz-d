package fuzzd.validator.executor

class CsExecutionHandler(fileDir: String, fileName: String): AbstractExecutionHandler(fileDir, fileName) {
    override fun getCompileTarget(): String = "cs"

    override fun getExecuteCommand(fileDir: String, fileName: String): String = "dotnet $fileDir/$fileName.dll"
}
