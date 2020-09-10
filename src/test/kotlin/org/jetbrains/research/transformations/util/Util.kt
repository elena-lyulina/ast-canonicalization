package org.jetbrains.research.transformations.util

import java.io.BufferedReader
import java.io.InputStreamReader


object Util {

     /*
      * Run ProcessBuilder and return output
      */
     fun runProcessBuilder(vararg command: String): String {
         val builder = ProcessBuilder(*command)
         builder.redirectErrorStream(true)
         val p = builder.start()
         return BufferedReader(InputStreamReader(p.inputStream)).readLines().joinToString(separator = "\n") { it }
    }

    fun getTmpPath(): String {
        val tmpPath = System.getProperty("java.io.tmpdir")
        return tmpPath.removeSuffix("/")
    }

}