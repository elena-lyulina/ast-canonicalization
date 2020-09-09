package org.jetbrains.research.transformations.util

import org.apache.commons.io.FileUtils
import org.jetbrains.research.transformations.util.ParserSetup.checkSetup
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.logging.Logger


/**
 * [ParserSetup] class is created for parser setup before using Gumtree with python code.
 * Also [checkSetup] should be called before running tests.
 */
object ParserSetup {
    private val logger = Logger.getLogger(javaClass.name)

    private const val PARSER_REPOSITORY_ZIP_URL = "https://github.com/JetBrains-Research/pythonparser/archive/master.zip"
    private const val PARSER_ZIP_NAME = "master.zip"

    // Relative path in the parser repository
    private const val PARSER_RELATIVE_PATH = "pythonparser-master/src/main/python/pythonparser/pythonparser_3.py"

    private const val PARSER_NAME = "pythonparser"
    private val TARGET_PARSER_PATH = "${getTmpPath()}/$PARSER_NAME"

    /**
     * Get parser repository path in this project in the resources folder
     */
    private fun getParserRepositoryPath(): String {
        val zipFilePath = Paths.get(javaClass.getResource(PARSER_ZIP_NAME).path)
        return zipFilePath.parent.toString()
    }

    /**
     * Unzip parser repository
     * @param toUpdate - it is true, if a new version of the parser needs to download
     */
    private fun unzipParserRepo(toUpdate: Boolean) {
        val zipFilePath = Paths.get(javaClass.getResource(PARSER_ZIP_NAME).path)
        if (toUpdate) {
            logger.info("Updating the current master zip")
            val file: InputStream = URL(PARSER_REPOSITORY_ZIP_URL).openStream()
            Files.copy(file, zipFilePath, StandardCopyOption.REPLACE_EXISTING)
        }
        logger.info("Unzipping the folder with the repository")
        ProcessBuilder()
            .command("unzip", "-o", zipFilePath.toString(), "-d", getParserRepositoryPath())
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start()
            .waitFor()
    }

    /**
     * Makes incoming file executable
     * @param targetFile - file, which file system permissions will be
     * changed to "executable".
     */
    private fun makeFileExecutable(targetFile: File) {
        logger.info("Making parser file executable")
        val command = arrayOf("chmod", "+x", targetFile.absolutePath)
        val builder = ProcessBuilder(*command)
        builder.directory(targetFile.parentFile)
        builder.start()
    }

    private fun getTmpPath(): String {
        val tmpPath = System.getProperty("java.io.tmpdir")
        return tmpPath.removeSuffix("/")
    }

    /**
     * Checks if parser file is in the target place and it is executable.
     * if not - makes it so.
     */
    fun checkSetup(toUpdateRepository: Boolean = false) {
        logger.info("Checking correctness of a parser setup")
        unzipParserRepo(toUpdateRepository)
        val repositoryPath = getParserRepositoryPath()
        val pythonparserFile = File("$repositoryPath/$PARSER_RELATIVE_PATH")
        val targetFile = File(TARGET_PARSER_PATH)
        if (!targetFile.exists() || !FileUtils.contentEquals(pythonparserFile, targetFile)) {
            logger.info("Parser file will be created in $TARGET_PARSER_PATH")
            pythonparserFile.copyTo(File(TARGET_PARSER_PATH), overwrite = true)
            makeFileExecutable(targetFile)
        }
        else {
            logger.info("Parser file already exists in $TARGET_PARSER_PATH")
        }
        logger.info("Adding pythonparser's path into system path")
        System.setProperty("gt.pp.path", TARGET_PARSER_PATH)
    }
}