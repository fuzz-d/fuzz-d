package fuzzd.validator.executor.execution_handler

import fuzzd.utils.readErrorStream
import fuzzd.utils.readInputStream
import fuzzd.utils.verifyDafny
import fuzzd.validator.executor.ExecutionResult
import fuzzd.validator.executor.execution_handler.AbstractExecutionHandler.Companion.TIMEOUT_RETURN_CODE
import java.util.concurrent.TimeUnit.SECONDS

class VerificationHandler(val fileDir: String, val fileName: String) : Runnable {
    private var verificationResult: ExecutionResult = ExecutionResult()

    fun verify(): ExecutionResult {
        val process = verifyDafny(fileDir, fileName, VERIFICATION_TIMEOUT)
        val termination = process.waitFor(VERIFICATION_TIMEOUT, SECONDS)

        return ExecutionResult(
            termination,
            if (termination) process.exitValue() else TIMEOUT_RETURN_CODE,
            process.readInputStream(),
            process.readErrorStream(),
        )
    }

    fun verificationResult() = verificationResult

    override fun run() {
        verificationResult = verify()
    }

    companion object {
        private const val VERIFICATION_TIMEOUT = 30L
    }
}
