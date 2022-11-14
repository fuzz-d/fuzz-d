package fuzzd.validator

import fuzzd.validator.executor.execution_handler.ExecutionHandler

data class ValidationResult(val erroneousResult: Boolean, val executed: List<ExecutionHandler>, val failedExecute: List<ExecutionHandler>, val failedCompile: List<ExecutionHandler>)
