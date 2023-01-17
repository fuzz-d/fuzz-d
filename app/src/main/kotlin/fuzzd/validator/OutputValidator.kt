package fuzzd.validator

import fuzzd.validator.executor.execution_handler.CsExecutionHandler
import fuzzd.validator.executor.execution_handler.GoExecutionHandler
import fuzzd.validator.executor.execution_handler.JavaExecutionHandler
import fuzzd.validator.executor.execution_handler.JsExecutionHandler
import fuzzd.validator.executor.execution_handler.PyExecutionHandler

class OutputValidator {
    /**
     * Runs the given file using supported target languages, and evaluates the outputs against each other
     * @param fileDir - the directory in which the file is located
     * @param fileName - the filename of the file excluding any file extensions
     */
    fun validateFile(fileDir: String, fileName: String): ValidationResult {
        val handlers = listOf(
            CsExecutionHandler(fileDir, fileName),
            JsExecutionHandler(fileDir, fileName),
            PyExecutionHandler(fileDir, fileName),
            JavaExecutionHandler(fileDir, fileName),
            GoExecutionHandler(fileDir, fileName)
        )

        handlers.map { Thread(it) }
            .map { t -> t.start(); t }
            .map { t -> t.join() }

        return ValidationResult(handlers)
    }
}
