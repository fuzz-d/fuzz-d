package fuzzd.validator.executor.execution_handler

import fuzzd.validator.executor.ExecutionResult

interface ExecutionHandler : Runnable {
    fun compile(): ExecutionResult
    fun compileResult(): ExecutionResult
    fun execute(): ExecutionResult
    fun executeResult(): ExecutionResult
    fun getCompileTarget(): String
}
