package fuzzd.logging

import java.io.File

class Logger(dir: File, fileName: String = FUZZD_LOG_NAME) {
    private val writer = OutputWriter(dir, fileName)

    fun <T> log(item: () -> T) {
        writer.write { item() }
        writer.write { "\n" }
    }

    fun close() = writer.close()

    companion object {
        const val FUZZD_LOG_NAME = "fuzz-d.log"
    }
}
