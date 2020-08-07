package org.jetbrains.research.transformations.util

import java.io.File
import java.util.logging.Logger

/**
 * [ParserSetup] class is created for parser setup before using Gumtree with python code.
 * Also [checkSetup] should be called before running tests.
 */
object ParserSetup {
    private val LOG = Logger.getLogger(javaClass.name)
    private const val PARSER_NAME = "pythonparser.py"
    private val TARGET_PATH = "${System.getProperty("java.io.tmpdir")}$PARSER_NAME"

    /**
     * Puts parser file to the target path.
     * @param targetPath - path to the final file destination,
     * where it will be putted.
     */
    private fun putParserToTargetPath(targetPath: String= TARGET_PATH) {

        try {
            LOG.info("Putting parser into $targetPath")

            val pythonparserFile = File(javaClass.getResource(PARSER_NAME).path)
            val targetFile = File(targetPath)
            pythonparserFile.copyTo(targetFile)

            // add pythonparser's path into system path
            System.setProperty("gt.pp.path", TARGET_PATH)

        } catch (e: FileAlreadyExistsException) {
            LOG.info("Parser file is already in $TARGET_PATH")
        }

    }

    /**
     * Makes incoming file executable
     * @param targetFile - file, which file system permissions will be
     * changed to "executable".
     */
    private fun makeFileExecutable(targetFile: File) {
        LOG.info("Making parser file executable")
        val command = arrayOf("chmod", "+x", targetFile.absolutePath)
        val builder = ProcessBuilder(*command)
        builder.directory(targetFile.parentFile)
        builder.start()
    }

    /**
     * Checks if parser file is in the target place and it is executable.
     * if not - makes it so.
     */
    fun checkSetup() {
        LOG.info("Checking correctness of a parser setup")
        val targetFile = File(TARGET_PATH)
        if (targetFile.exists()) {
            LOG.info("Parser file already exists in $TARGET_PATH")
            return
        }
        else {
            LOG.info("Parser file will be created in $TARGET_PATH")
            putParserToTargetPath()
            makeFileExecutable(targetFile)
        }
    }
}
