package org.jetbrains.research.transformations

import PythonTransformationsTest
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.tree.TreeContext

import org.jetbrains.research.transformations.util.ParserSetup
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File

/*
* Check if parser setup works correctly
* */
internal class PythonSetupTest : PythonTransformationsTest() {
    private val srcFile: String = javaClass.getResource("source.py").path
    private val parserRepoFile = File(ParserSetup.parserRepoPath)
    private val parserZipFile = File(ParserSetup.parserZipPath)
    private val targetParserFile = File(ParserSetup.targetParserPath)

    private fun getTreeContext(): TreeContext {
        return PythonTreeGenerator().generateFromFile(srcFile)
    }

    private fun getLastModified(): List<Long> {
       return listOf(parserZipFile, parserRepoFile, targetParserFile).map { it.lastModified() }
    }

    @Test
    fun `check if parser setup works without both parser zip and parser repo`() {
        parserRepoFile.deleteRecursively()
        parserZipFile.deleteRecursively()
        ParserSetup.checkSetup()
//      Parser zip file should be updated and parser repo should be unzipped
        assert(parserZipFile.exists())
        assert(parserRepoFile.exists())
        assertDoesNotThrow(::getTreeContext)

    }

    @Test
    fun `check if parser setup works without parser repo`() {
        assert(parserZipFile.exists())
        parserRepoFile.deleteRecursively()

        val expectedLastModified = parserZipFile.lastModified()
        ParserSetup.checkSetup()
        val actualLastModified = parserZipFile.lastModified()

//      Parser repo file should be unzipped from existing zip file, which shouldn't be changed
        assert(parserRepoFile.exists())
        assert(expectedLastModified == actualLastModified)
        assertDoesNotThrow(::getTreeContext)
    }

    @Test
    fun `check if parser setup works without parser zip`() {
        assert(parserRepoFile.exists())
        parserZipFile.deleteRecursively()

        val expectedLastModified = parserRepoFile.lastModified()
        ParserSetup.checkSetup()
        val actualLastModified = parserRepoFile.lastModified()

//      Parser zip file shouldn't exists if there was parser repo, which shouldn't be changed
        assert(!parserZipFile.exists())
        assert(expectedLastModified == actualLastModified)
        assertDoesNotThrow(::getTreeContext)
    }

    @Test
    fun `check if updates works with already existing parser zip and repo`() {
        assert(parserRepoFile.exists())
        assert(parserZipFile.exists())

        val lastModifiedBeforeSetup = getLastModified()
        println(lastModifiedBeforeSetup)
        ParserSetup.checkSetup(true)
        val lastModifiedAfterSetup = getLastModified()
        println(lastModifiedAfterSetup)

//      Parser zip file and repo should exist and should be changed together with targetParserFile
        assert(parserRepoFile.exists())
        assert(parserRepoFile.exists())
        assert(lastModifiedBeforeSetup.zip(lastModifiedAfterSetup).all { it.first != it.second })
        assertDoesNotThrow(::getTreeContext)
    }
}