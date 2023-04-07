package fuzzd.logging

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.Writer
import kotlin.system.exitProcess

class OutputWriter(dir: File, fileName: String) {
    private val writer: Writer
    val dirPath: String

    init {
        // create directory if not exists and open file writer
        dir.mkdir()

        // create file in directory
        val file = File("${dir.path}/$fileName")

        // open writer and store path
        writer = BufferedWriter(FileWriter(file.absoluteFile))
        dirPath = dir.absolutePath
    }

    fun <T> write(item: () -> T) {
        try {
            writer.write(item().toString())
        } catch (e: IOException) {
            e.printStackTrace()
            exitProcess(-1)
        }
    }

    fun close() = writer.close()
}
