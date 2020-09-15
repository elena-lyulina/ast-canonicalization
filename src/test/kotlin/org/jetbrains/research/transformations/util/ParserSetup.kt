package org.jetbrains.research.transformations.util

import org.jetbrains.research.transformations.util.ParserSetup.checkSetup
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.logging.Logger
import net.lingala.zip4j.ZipFile
import org.apache.commons.lang3.SystemUtils
import org.jetbrains.research.transformations.util.Util.getTmpPath
import org.jetbrains.research.transformations.util.Util.runProcessBuilder


/**
 * [ParserSetup] class is created for parser setup before using Gumtree with python code.
 * Also [checkSetup] should be called before running tests.
 */
object ParserSetup {
    private val LOG = Logger.getLogger(javaClass.name)

    private const val PYTHON3_PROPERTY = "ac.p3.path"
    private const val PYTHONPARSER_PROPERTY = "gt.pp.path"

    private const val PARSER_REPOSITORY_ZIP_URL =
        "https://github.com/JetBrains-Research/pythonparser/archive/master.zip"
    private const val PARSER_ZIP_NAME = "master.zip"

    // Relative path in the parser repository
    private const val REPOSITORY_ROOT_FOLDER = "pythonparser-master"
    private const val PARSER_RELATIVE_PATH = "$REPOSITORY_ROOT_FOLDER/src/main/python/pythonparser/pythonparser_3.py"
    private const val INVERSE_PARSER_RELATIVE_PATH =
        "$REPOSITORY_ROOT_FOLDER/src/main/python/inverse_parser/inverse_parser_3.py"

    private const val PARSER_NAME = "pythonparser"

    private val targetParserPath = "${getTmpPath()}/$PARSER_NAME"
    private val targetInverseParserPath = "${getParserRepositoryPath()}/$INVERSE_PARSER_RELATIVE_PATH"

    /**
     * Runs inverse_parser_3 using python3, which it gets from [PYTHON3_PROPERTY] or sets the default one
     * To set [PYTHON3_PROPERTY], please, add -P[PYTHON3_PROPERTY]=/path/to/python3 to the command line
     */
    fun getCommandForInverseParser(XMLPath: String): Util.Command {
        val defaultPythonBin = if (SystemUtils.IS_OS_WINDOWS) "where python3" else "which python3"
        val pythonBin = System.getProperty(PYTHON3_PROPERTY)?.let { "echo $it" } ?: defaultPythonBin
        return Util.Command(
            listOf("/bin/bash", "-c", "$($pythonBin) $targetInverseParserPath $XMLPath"),
            environment = mapOf("PYTHONPATH" to getRepositoryRootPath())
        )
    }

    private fun getRepositoryRootPath() =
        "${getParserRepositoryPath()}/$REPOSITORY_ROOT_FOLDER"

    private fun getRepositoryZipPath() =
        "${getParserRepositoryPath()}/$PARSER_ZIP_NAME"

    /**
     * Get parser repository path in this project in the resources folder
     */
    private fun getParserRepositoryPath(): String {
        val zipFilePath = Paths.get(javaClass.getResource(PARSER_ZIP_NAME).path)
        // TODO: find a better way for it
        return zipFilePath.parent.toString().replace("build/resources/test/", "src/test/resources/")
    }

    /**
     * Update parser repository
     * @param toUpdate - it is true, if a new version of the parser needs to download
     */
    private fun updateParserRepo(toUpdate: Boolean) {
        val zipFilePath = Paths.get("${getParserRepositoryPath()}/$PARSER_ZIP_NAME")
        if (toUpdate) {
            LOG.info("Updating the current master zip")
            val file: InputStream = URL(PARSER_REPOSITORY_ZIP_URL).openStream()
            Files.copy(file, zipFilePath, StandardCopyOption.REPLACE_EXISTING)
        }
        unzipParserRepo()
    }

    private fun unzipParserRepo(zipParserRepoPath: String = getRepositoryZipPath()) {
        LOG.info("Unzipping the folder with the repository")
        val zipFile = ZipFile(zipParserRepoPath)
        val parserRepositoryPath = getParserRepositoryPath()
        zipFile.extractAll(parserRepositoryPath)
    }

    /**
     * Makes incoming file executable
     * @param targetFile - file, which file system permissions will be
     * changed to "executable".
     */
    private fun makeFileExecutable(targetFile: File) {
        LOG.info("Making parser file executable")
        runProcessBuilder(Util.Command(listOf("chmod", "+x", targetFile.absolutePath)))
    }

    /**
     * Check if parser file is in the target place and it is executable.
     * if not - makes it so.
     */
    private fun checkParserFile(
        parserFilePath: String, targetPath: String, toUpdateRepository: Boolean = false,
        toAddIntoSystemPath: Boolean = true
    ) {
        val targetFile = File(targetPath)
        if (!targetFile.exists() || toUpdateRepository) {
            LOG.info("Parser file will be created in $targetPath")
            val parserFile = File(parserFilePath)
            updateParserRepo(toUpdateRepository)
            parserFile.copyTo(File(targetPath), overwrite = true)
            makeFileExecutable(targetFile)
        } else {
            LOG.info("Parser file already exists in $targetPath")
        }
        if (toAddIntoSystemPath) {
            LOG.info("Adding parser path into system path")
            System.setProperty(PYTHONPARSER_PROPERTY, targetPath)
        }
    }

    private fun isParserRepositoryRootFolderExist(): Boolean {
        val repoFolder = File("${getParserRepositoryPath()}/$REPOSITORY_ROOT_FOLDER")
        return repoFolder.exists()
    }

    /*
     * Check if pythonparser and inverse parser files is valid
     */
    fun checkSetup(toUpdateRepository: Boolean = false) {
        LOG.info("Checking correctness of a parser setup")
        val repositoryPath = getParserRepositoryPath()
        if (!isParserRepositoryRootFolderExist()) {
            unzipParserRepo()
        }
        checkParserFile("$repositoryPath/$PARSER_RELATIVE_PATH", targetParserPath, toUpdateRepository)
    }
}