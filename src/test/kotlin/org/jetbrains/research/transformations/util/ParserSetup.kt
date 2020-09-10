package org.jetbrains.research.transformations.util

import com.github.gumtreediff.gen.python.PythonTreeGenerator
import org.jetbrains.research.transformations.util.ParserSetup.checkSetup
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.logging.Logger
import net.lingala.zip4j.ZipFile
import org.jetbrains.research.transformations.util.Util.getTmpPath


/**
 * [ParserSetup] class is created for parser setup before using Gumtree with python code.
 * Also [checkSetup] should be called before running tests.
 */
object ParserSetup {
    private val LOG = Logger.getLogger(javaClass.name)

    private const val PARSER_REPOSITORY_ZIP_URL = "https://github.com/JetBrains-Research/pythonparser/archive/master.zip"
    private const val PARSER_ZIP_NAME = "master.zip"

    // Relative path in the parser repository
    private const val PARSER_RELATIVE_PATH = "pythonparser-master/src/main/python/pythonparser/pythonparser_3.py"
    private const val INVERSE_PARSER_RELATIVE_PATH = "pythonparser-master/src/main/python/inverse_parser/inverse_parser_3.py"

    private const val PARSER_NAME = "pythonparser"
    private const val INVERSE_PARSER_NAME = "inverse_parser_3.py"

    private val TARGET_PARSER_PATH = "${getTmpPath()}/$PARSER_NAME"
    private val TARGET_INVERSE_PARSER_PATH = "${getTmpPath()}/$INVERSE_PARSER_NAME"

    fun getCommandForInverseParser(XMLPath: String): Array<String> {
        return arrayOf("python3", TARGET_INVERSE_PARSER_PATH, XMLPath)
    }

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
            LOG.info("Updating the current master zip")
            val file: InputStream = URL(PARSER_REPOSITORY_ZIP_URL).openStream()
            Files.copy(file, zipFilePath, StandardCopyOption.REPLACE_EXISTING)
        }
        LOG.info("Unzipping the folder with the repository")
        val zipFile = ZipFile(zipFilePath.toString())
        zipFile.extractAll(getParserRepositoryPath())
    }

    /**
     * Makes incoming file executable
     * @param targetFile - file, which file system permissions will be
     * changed to "executable".
     */
    private fun makeFileExecutable(targetFile: File) {
        LOG.info("Making parser file executable")
        Util.runProcessBuilder("chmod", "+x", targetFile.absolutePath)
    }

    /**
     * Check if parser file is in the target place and it is executable.
     * if not - makes it so.
     */
    private fun checkParserFile(parserFilePath: String, targetPath: String, toUpdateRepository: Boolean = false) {
        val targetFile = File(targetPath)
        if (!targetFile.exists() || toUpdateRepository) {
            LOG.info("Parser file will be created in $targetPath")
            val parserFile = File(parserFilePath)
            unzipParserRepo(toUpdateRepository)
            parserFile.copyTo(File(targetPath), overwrite = true)
            makeFileExecutable(targetFile)
        }
        else {
            LOG.info("Parser file already exists in $targetPath")
        }
        LOG.info("Adding parser path into system path")
        System.setProperty("gt.pp.path", targetPath)
    }

    /*
     * Check if pythonparer and inverse parser files is valid
     */
    fun checkSetup(toUpdateRepository: Boolean = false) {
        LOG.info("Checking correctness of a parser setup")
        val repositoryPath = getParserRepositoryPath()

        checkParserFile("$repositoryPath/$PARSER_RELATIVE_PATH", TARGET_PARSER_PATH, toUpdateRepository)
        checkParserFile("$repositoryPath/$INVERSE_PARSER_RELATIVE_PATH", TARGET_INVERSE_PARSER_PATH, toUpdateRepository)
    }
}

fun main() {
    val srcFile = "/Users/Anastasiia.Birillo/PycharmProjects/pythonparser/src/main/python/pythonparser/test.py"
    println(srcFile)
    val treeCtx = PythonTreeGenerator().generateFromFile(srcFile)
}