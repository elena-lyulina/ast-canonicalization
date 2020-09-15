package org.jetbrains.research.transformations

import PythonTransformationsTest
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.transformations.util.Util.getContentFromFile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File

/*
* Just check if the parsers work correctly
* */
internal class PythonTransformationSetupTest: PythonTransformationsTest() {
    private val SRC_FILE: String = javaClass.getResource("source.py").path

    private fun getTreeContext(): TreeContext {
        return PythonTreeGenerator().generateFromFile(SRC_FILE)
    }

    @Test
    fun checkParserSetup() {
        assertDoesNotThrow(::getTreeContext)
    }

    @Test
    fun checkInverseParserSetup() {
        val treeCtx = getTreeContext()
        val expectedOutput = getContentFromFile(File(SRC_FILE))
        val actualOutput = getPythonSourceCode(treeCtx, XMLTreeFileName)
        Assertions.assertEquals(expectedOutput, actualOutput)
    }
}