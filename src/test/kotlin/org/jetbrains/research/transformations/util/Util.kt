package org.jetbrains.research.transformations.util

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


object Util {

    /*
     * Run ProcessBuilder and return output
     */
    fun runProcessBuilder(command: List<String>, runningDirectory: String? = null): String {
        val builder = ProcessBuilder(command)
        runningDirectory?.let { builder.directory(File(it)) }
        builder.redirectErrorStream(true)
        val p = builder.start()
        return BufferedReader(InputStreamReader(p.inputStream)).readLines().joinToString(separator = "\n") { it }
    }

    fun getPython3Path(): String {
        if (isWindows()) {
            runProcessBuilder(listOf("where", "python3"))
        }
        return runProcessBuilder(listOf("which", "python3"))
    }

    fun getTmpPath(): String {
        val tmpPath = System.getProperty("java.io.tmpdir")
        return tmpPath.removeSuffix("/")
    }

    fun getContentFromFile(file: File): String {
        return file.readLines().joinToString(separator = "\n") { it }
    }

    private fun isWindows() = System.getProperty("os.name").toLowerCase().contains("windows")

}