package fuzzd.validator.executor.execution_handler

class PyExecutionHandler(fileDir: String, fileName: String) : AbstractExecutionHandler(fileDir, fileName) {
    override fun getCompileTarget(): String = "py"

    override fun getExecuteCommand(fileDir: String, fileName: String): String =
        "python3 $fileDir/$fileName-py/$fileName.py"
}
