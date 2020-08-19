package org.jetbrains.research.transformations.util

import com.github.gumtreediff.gen.Generators
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.io.TreeIoUtils
import java.io.File
import java.util.logging.Logger
import org.apache.commons.io.FileUtils

/**
 * [ParserSetup] class is created for parser setup before using Gumtree with python code.
 * Also [checkSetup] should be called before running tests.
 */
object ParserSetup {
    private val LOG = Logger.getLogger(javaClass.name)
    private const val PARSER_NAME = "pythonparser"
    private val TARGET_PATH = "${getTmpPath()}/$PARSER_NAME"

    /**
     * Puts parser file to the target path.
     * @param targetPath - path to the final file destination,
     * where it will be putted.
     */
    private fun putParserToTargetPath(targetPath: String= TARGET_PATH) {

        LOG.info("Putting parser into $targetPath")
        val pythonparserFile = File(javaClass.getResource("$PARSER_NAME.py").path)
        pythonparserFile.copyTo(File(targetPath), overwrite = true)

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
        val pythonparserFile = File(javaClass.getResource("$PARSER_NAME.py").path)
        val targetFile = File(TARGET_PATH)
        if (!targetFile.exists() or !FileUtils.contentEquals(pythonparserFile, targetFile)) {
            LOG.info("Parser file will be created in $TARGET_PATH")
            putParserToTargetPath()
            makeFileExecutable(targetFile)
        }
        else {
            LOG.info("Parser file already exists in $TARGET_PATH")
        }
        // add pythonparser's path into system path
        System.setProperty("gt.pp.path", TARGET_PATH)
    }

    private fun getTmpPath(): String {
        val tmpPath = System.getProperty("java.io.tmpdir")
        return tmpPath.removeSuffix("/")
    }
}
