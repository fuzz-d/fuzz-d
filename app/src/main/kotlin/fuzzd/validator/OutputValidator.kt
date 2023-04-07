package fuzzd.validator

import fuzzd.logging.OutputWriter
import fuzzd.validator.executor.execution_handler.CsExecutionHandler
import fuzzd.validator.executor.execution_handler.ExecutionHandler
import fuzzd.validator.executor.execution_handler.GoExecutionHandler
import fuzzd.validator.executor.execution_handler.JavaExecutionHandler
import fuzzd.validator.executor.execution_handler.JsExecutionHandler
import fuzzd.validator.executor.execution_handler.PyExecutionHandler
import java.io.File

class OutputValidator {
    /**
     * Runs the given file using supported target languages, and evaluates the outputs against each other
     * @param fileDir - the directory in which the file is located
     * @param wrapperFileName - the filename of the file containing the wrappers
     * @param bodyFileName - the filename of the file containing the body of the dafny code
     * @param mainFileName - the filename of the file the wrapper and body files should be combined into
     */
    fun validateFile(
        fileDir: File,
        wrapperFileName: String,
        bodyFileName: String,
        mainFileName: String,
    ): ValidationResult {
        val writer = OutputWriter(fileDir, "$mainFileName.dfy")
        val fileDirPath = fileDir.path

        writer.write { File("$fileDirPath/$wrapperFileName.dfy").readText() }
        writer.write { File("$fileDirPath/$bodyFileName.dfy").readText() }

        writer.close()

        val handlers = listOf(
            CsExecutionHandler(fileDirPath, mainFileName),
            JsExecutionHandler(fileDirPath, mainFileName),
            PyExecutionHandler(fileDirPath, mainFileName),
            JavaExecutionHandler(fileDirPath, mainFileName),
            GoExecutionHandler(fileDirPath, mainFileName),
        )

        handlers.map { Thread(it) }
            .map { t -> t.start(); t }
            .map { t -> t.join() }

        return ValidationResult(handlers)
    }

    fun collectOutput(handler: ExecutionHandler): String? {
        handler.run()
        return if (handler.executeResult().terminated) {
            println(ValidationResult(listOf(handler)))
            handler.executeResult().stdOut
        } else {
            null
        }
    }
}
