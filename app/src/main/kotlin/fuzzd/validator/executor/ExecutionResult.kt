package fuzzd.validator.executor

data class ExecutionResult(
    val terminated: Boolean = true,
    val exitCode: Int = -1,
    val stdOut: String = "",
    val stdErr: String = ""
) {
    override fun toString(): String {
        val sb = StringBuilder()

        sb.appendLine("Terminated: $terminated")
        sb.appendLine("Exit code: $exitCode")
        sb.appendLine("StdOut: $stdOut")
        sb.appendLine("StdErr: $stdErr")

        return sb.toString()
    }
}
