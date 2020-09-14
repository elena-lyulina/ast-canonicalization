package org.jetbrains.research.transformations.util

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


object Util {

    /*
     * Run ProcessBuilder and return output
     */
    fun runProcessBuilder(command: List<String>, runningDirectory: String? = null, variables: Map<String, String>? = null): String {
        val builder = ProcessBuilder(command)
        val envs: MutableMap<String, String> = builder.environment()
        variables?.let {
            val environment = builder.environment()
            variables.entries.forEach { e -> environment[e.key] = e.value }
        }
        runningDirectory?.let { builder.directory(File(it)) }
        builder.redirectErrorStream(true)
        val p = builder.start()
        return BufferedReader(InputStreamReader(p.inputStream)).readLines().joinToString(separator = "\n") { it }
    }

    fun getPython3Path(): String {
        val builder = ProcessBuilder("which", "python3")
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