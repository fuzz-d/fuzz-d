package fuzzd.validator

import fuzzd.validator.executor.ExecutionResult
import fuzzd.validator.executor.execution_handler.CsExecutionHandler
import fuzzd.validator.executor.execution_handler.GoExecutionHandler
import fuzzd.validator.executor.execution_handler.JavaExecutionHandler
import fuzzd.validator.executor.execution_handler.JsExecutionHandler
import fuzzd.validator.executor.execution_handler.PyExecutionHandler
import fuzzd.validator.executor.execution_handler.VerificationHandler
import java.io.File

class OutputValidator {
    fun validateFile(
        fileDir: File,
        mainFileName: String,
        targetOutput: String?,
        verifier: Boolean,
    ): ValidationResult {
        val fileDirPath = fileDir.path

        val executionHandlers = listOf(
            CsExecutionHandler(fileDirPath, mainFileName),
            JsExecutionHandler(fileDirPath, mainFileName),
            PyExecutionHandler(fileDirPath, mainFileName),
            JavaExecutionHandler(fileDirPath, mainFileName),
            GoExecutionHandler(fileDirPath, mainFileName),
        )

        return if (verifier) {
            val verificationHandler = VerificationHandler(fileDirPath, mainFileName)
            execute(executionHandlers + verificationHandler)

            ValidationResult(executionHandlers, verificationHandler, targetOutput)
        } else {
            execute(executionHandlers)

            ValidationResult(executionHandlers, null, targetOutput)
        }
    }

    fun verifyFiles(fileDir: File, fileNames: List<String>): List<ExecutionResult> {
        val verificationHandlers = fileNames.map { VerificationHandler(fileDir.path, it) }
        execute(verificationHandlers)

        return verificationHandlers.map { it.verificationResult() }
    }

    private fun execute(runnables: List<Runnable>) {
        runnables.map { Thread(it) }
            .map { t -> t.start(); t }
            .map { t -> t.join() }
    }
}
