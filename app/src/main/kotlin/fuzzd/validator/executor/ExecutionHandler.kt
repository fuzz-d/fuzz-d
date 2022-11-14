package fuzzd.validator.executor

interface ExecutionHandler: Runnable {
    fun compile(): ExecutionResult
    fun compileResult(): ExecutionResult
    fun execute(): ExecutionResult
    fun executeResult(): ExecutionResult
}
