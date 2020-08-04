package org.jetbrains.research.transformations

import java.io.File
import java.util.logging.Logger

object SetupParser {
    val LOG = Logger.getLogger(this.javaClass.name)
    private const val TARGET_PATH = "/tmp/pythonparser"

    fun putParserToTmp() {
        //copy parser file into /tmp directory, required by Gumtree
        try {
            LOG.info("Putting parser into $TARGET_PATH")

            val pythonparserFile = File(javaClass.getResource("./parser/pythonparser.py").path)
            val targetFile = File(TARGET_PATH)

            pythonparserFile.copyTo(targetFile)
            makeFileExecutable()
        } catch (e: FileAlreadyExistsException) {
            LOG.info("Parser file is already in $TARGET_PATH")
        }

    }

    fun makeFileExecutable() {
        LOG.info("Making parser file executable")
        val targetFile = File(TARGET_PATH)
        val command = arrayOf("chmod", "+x", targetFile.absolutePath)
        val builder = ProcessBuilder(*command)
        builder.directory(targetFile.parentFile)
        builder.start()

        // add pythonparser's path into system path
        System.setProperty("gt.pp.path", TARGET_PATH)
    }

    fun checkSetup() {
        LOG.info("Checking correctness of a parser setup")
        val targetFile = File(TARGET_PATH)
        if (targetFile.exists()) {
            LOG.info("Parser file already exists in $TARGET_PATH")
            return
        }
        else {
            LOG.info("Parser file will be created in $TARGET_PATH")
            putParserToTmp()
        }
    }
}
