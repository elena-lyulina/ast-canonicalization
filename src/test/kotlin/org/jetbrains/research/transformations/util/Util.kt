package org.jetbrains.research.transformations.util

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


object Util {

    /**
     * Represents a command passed to the [ProcessBuilder], where
     * [command] is a command to run (see [ProcessBuilder.command]),
     * [directory] is a working directory (see [ProcessBuilder.directory]),
     * and [environment] contains environment variables (see [ProcessBuilder.environment]).
     */
    data class Command(val command: List<String>, val directory: String? = null, val environment: Map<String, String>? = null)

    /*
     * Run ProcessBuilder and return output
     */
    fun runProcessBuilder(command: Command): String {
        val builder = ProcessBuilder(command.command)
        command.environment?.let {
            val environment = builder.environment()
            it.entries.forEach { e -> environment[e.key] = e.value }
        }
        command.directory?.let { builder.directory(File(it)) }
        builder.redirectErrorStream(true)
        val p = builder.start()
        return BufferedReader(InputStreamReader(p.inputStream)).readLines().joinToString(separator = "\n") { it }
    }

    fun getTmpPath(): String {
        val tmpPath = System.getProperty("java.io.tmpdir")
        return tmpPath.removeSuffix("/")
    }

    fun getContentFromFile(file: File): String {
        return file.readLines().joinToString(separator = "\n") { it }.removeSuffix("\n")
    }
}