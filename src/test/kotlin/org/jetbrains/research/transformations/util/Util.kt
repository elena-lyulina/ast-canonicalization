package org.jetbrains.research.transformations.util

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.logging.Logger


object Util {

    /*
     * Run ProcessBuilder and return output
     */
    fun runProcessBuilder(vararg command: String, runningDirectory: String?): String {
        val builder = ProcessBuilder(*command)
        runningDirectory?.let { builder.directory(File(it)) }
        builder.redirectErrorStream(true)
        val p = builder.start()
        return BufferedReader(InputStreamReader(p.inputStream)).readLines().joinToString(separator = "\n") { it }
    }

    fun getTmpPath(): String {
        val tmpPath = System.getProperty("java.io.tmpdir")
        return tmpPath.removeSuffix("/")
    }

    fun getContentFromFile(file: File): String {
        return file.readLines().joinToString(separator = "\n") { it }
    }

}