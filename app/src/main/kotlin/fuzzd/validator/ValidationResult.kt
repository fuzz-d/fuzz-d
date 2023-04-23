package fuzzd.validator

import fuzzd.utils.indent
import fuzzd.validator.executor.execution_handler.ExecutionHandler
import fuzzd.validator.executor.execution_handler.JavaExecutionHandler

class ValidationResult(handlers: List<ExecutionHandler>, val targetOutput: String?) {
    private val succeededCompile: List<ExecutionHandler>
    private val failedCompile: List<ExecutionHandler>
    private val succeededExecute: List<ExecutionHandler>
    private val failedExecute: List<ExecutionHandler>

    init {
        succeededCompile = handlers.filter { h ->
            val c = h.compileResult(); c.terminated && c.exitCode == 0
        }

        failedCompile = handlers.filter { h ->
            val c = h.compileResult(); !c.terminated || c.exitCode != 0
        }

        succeededExecute = succeededCompile.filter { h ->
            val e = h.executeResult(); e.terminated && e.exitCode == 0
        }

        failedExecute = handlers.filter { h ->
            val e = h.executeResult(); !e.terminated || e.exitCode != 0
        }
    }

    private fun differentOutput(): Boolean =
        succeededExecute.any { h ->
            h.executeResult().stdOut != (targetOutput ?: succeededExecute[0].executeResult().stdOut)
        }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.appendLine("--------------------------------- COMPILE FAILED -------------------------------")
        failedCompile.forEach { h -> sb.append("${h.getCompileTarget()}:\n${indent(h.compileResult())} \n") }

        sb.appendLine("--------------------------------- EXECUTE FAILED -------------------------------")
        failedExecute.forEach { h -> sb.append("${h.getCompileTarget()}:\n${indent(h.executeResult())}\n") }

        sb.appendLine("--------------------------------- EXECUTE SUCCEEDED -------------------------------")
        sb.appendLine("Target Output: $targetOutput")
        succeededExecute.forEach { h -> sb.append("${h.getCompileTarget()}:\n${indent(h.executeResult())}\n") }

        sb.appendLine("Java crash: ${failedCompile.filterIsInstance<JavaExecutionHandler>().isNotEmpty()}")
        sb.appendLine("Compiler crash: ${failedCompile.filter { it !is JavaExecutionHandler }.isNotEmpty()}")
        sb.appendLine("Execute crash: ${failedExecute.isNotEmpty()}")
        sb.appendLine("Different output: ${differentOutput()}")

        return sb.toString()
    }
}
