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

    private const val PARSER_REPO_ZIP_URL =
        "https://github.com/JetBrains-Research/pythonparser/archive/master.zip"
    private const val PARSER_ZIP_NAME = "master.zip"

    // Relative path in the parser repository
    private const val PARSER_REPO_FOLDER = "pythonparser-master"
    private const val PARSER_RELATIVE_PATH = "$PARSER_REPO_FOLDER/src/main/python/pythonparser/pythonparser_3.py"
    private const val INVERSE_PARSER_RELATIVE_PATH =
        "$PARSER_REPO_FOLDER/src/main/python/inverse_parser/inverse_parser_3.py"

    const val PARSER_NAME = "pythonparser"
    val targetParserPath = "${getTmpPath()}/$PARSER_NAME"
    val targetInverseParserPath = "${getParserRepoParentFolder()}/$INVERSE_PARSER_RELATIVE_PATH"
    val parserRepoPath = "${getParserRepoParentFolder()}/$PARSER_REPO_FOLDER"
    val parserZipPath = "${getParserRepoParentFolder()}/$PARSER_ZIP_NAME"

    /**
     * Runs inverse_parser_3 using python3, which it gets from [PYTHON3_PROPERTY] or sets the default one
     * To set [PYTHON3_PROPERTY], please, add -P[PYTHON3_PROPERTY]=/path/to/python3 to the command line
     */
    fun getCommandForInverseParser(XMLPath: String): Util.Command {
        val defaultPythonBin = if (SystemUtils.IS_OS_WINDOWS) "where python3" else "which python3"
        val pythonBin = System.getProperty(PYTHON3_PROPERTY)?.let { "echo $it" } ?: defaultPythonBin
        return Util.Command(
            listOf("/bin/bash", "-c", "$($pythonBin) $targetInverseParserPath $XMLPath"),
            environment = mapOf("PYTHONPATH" to parserRepoPath)
        )
    }

    /**
     * Get a parent folder of parser repository, which is stored in this project in the resources folder
     */
    private fun getParserRepoParentFolder(): String {
        val parserParentFolder = javaClass.getResource(PARSER_NAME).path
        // TODO: find a better way for it
        return parserParentFolder.replace("build/resources/test/", "src/test/resources/")
    }

    /**
     * Update parser zip file
     */
    private fun updateParserZip() {
        val zipFilePath = Paths.get("${getParserRepoParentFolder()}/$PARSER_ZIP_NAME")
        LOG.info("Updating the current master zip")
        val file: InputStream = URL(PARSER_REPO_ZIP_URL).openStream()
        Files.copy(file, zipFilePath, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun unzipParserRepo(zipParserRepoPath: String = parserZipPath) {
        LOG.info("Unzipping the folder with the repository")
        val zipFile = ZipFile(zipParserRepoPath)
        val parserRepositoryPath = getParserRepoParentFolder()
        File(parserRepoPath).deleteRecursively()
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
        parserFilePath: String,
        targetPath: String,
        toUpdateTargetFile: Boolean,
        toAddIntoSystemPath: Boolean = true
    ) {
        val targetFile = File(targetPath)
        if (!targetFile.exists() || toUpdateTargetFile) {
            LOG.info("Parser file will be created in $targetPath")
            val parserFile = File(parserFilePath)
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

    private fun String.exists() : Boolean {
        return File(this).exists()
    }

    /*
     * Check if pythonparser and inverse parser files are valid
     */
    fun checkSetup(toUpdateRepo: Boolean = false) {
        LOG.info("Checking correctness of a parser setup")
        val parserPath = "${getParserRepoParentFolder()}/$PARSER_RELATIVE_PATH"

        if (toUpdateRepo || !parserPath.exists()) {
            LOG.info("There is no parser repo (${!parserPath.exists()}) or it needs to be updated ($toUpdateRepo)")
            if (toUpdateRepo || !parserZipPath.exists()) {
                LOG.info("There is no parser zip (${!parserZipPath.exists()}) or it needs to be updated ($toUpdateRepo)")
                updateParserZip()
            }
            unzipParserRepo()
        }
        checkParserFile(parserPath, targetParserPath, toUpdateRepo)
    }
}