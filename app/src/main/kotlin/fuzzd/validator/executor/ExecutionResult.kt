package fuzzd.validator.executor

data class ExecutionResult(
    val terminated: Boolean = true,
    val exitCode: Int = -1,
    val stdOut: String = "",
    val stdErr: String = ""
)
