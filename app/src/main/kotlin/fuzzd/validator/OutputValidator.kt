package fuzzd.validator

import fuzzd.validator.executor.CsExecutionHandler
import fuzzd.validator.executor.ExecutionHandler

class OutputValidator {
    /**
     * Runs the given file using supported target languages, and evaluates the outputs against each other
     * @param fileDir - the directory in which the file is located
     * @param fileName - the filename of the file excluding any file extensions
     */
    fun validateFile(fileDir: String, fileName: String): ValidationResult {
        val handlers = listOf<ExecutionHandler>(CsExecutionHandler(fileDir, fileName))

        handlers.map { Thread(it) }
            .map{ t -> t.start(); t }
            .map{ t -> t.join() }

        val (succeededCompile, failedCompile) = handlers.partition { h -> val c = h.compileResult(); c.terminated && c.exitCode == 0 }
        val (succeededExecute, failedExecute) = succeededCompile.partition { h -> val e = h.executeResult(); e.terminated && e.exitCode == 0 }

        val erroneousResult = failedCompile.isNotEmpty() || failedExecute.isNotEmpty() || !succeededExecute.all { h -> h.executeResult().stdOut == succeededExecute[0].executeResult().stdOut }

        return ValidationResult(erroneousResult, succeededExecute, failedExecute, failedCompile)
    }
}
