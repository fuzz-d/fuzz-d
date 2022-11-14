package fuzzd.validator

import fuzzd.validator.executor.ExecutionHandler

data class ValidationResult(val erroneousResult: Boolean, val executed: List<ExecutionHandler>, val failedExecute: List<ExecutionHandler>, val failedCompile: List<ExecutionHandler>)
