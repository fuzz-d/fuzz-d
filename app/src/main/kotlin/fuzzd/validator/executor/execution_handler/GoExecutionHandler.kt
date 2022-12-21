package fuzzd.validator.executor.execution_handler

class GoExecutionHandler(fileDir: String, fileName: String) : AbstractExecutionHandler(fileDir, fileName) {
    override fun getCompileTarget(): String = "go"

    override fun getExecuteCommand(fileDir: String, fileName: String): String = "$fileDir/$fileName"
}
