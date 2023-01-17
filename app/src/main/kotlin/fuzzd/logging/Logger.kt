package fuzzd.logging

class Logger(path: String, directory: String) {
    private val writer = OutputWriter(path, directory, FUZZD_LOG_NAME)

    fun <T> log(item: () -> T) {
        writer.write { item() }
        writer.write { "\n" }
    }

    fun close() = writer.close()

    companion object {
        const val FUZZD_LOG_NAME = "fuzz-d.log"
    }
}
