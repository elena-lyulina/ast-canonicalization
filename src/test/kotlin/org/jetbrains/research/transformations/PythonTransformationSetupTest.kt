package org.jetbrains.research.transformations

import PythonTransformationsTest
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.tree.TreeContext
import org.junit.jupiter.api.Test

/*
* Just check if the parsers work correct
* */
internal class PythonTransformationSetupTest: PythonTransformationsTest() {

    private fun getTreeContext(): TreeContext {
        val srcFile = javaClass.getResource("source.py").path
        return PythonTreeGenerator().generateFromFile(srcFile)
    }

    @Test
    fun checkParserSetup() {
        getTreeContext()
    }

    @Test
    fun checkInverseParserSetup() {
        val treeCtx = getTreeContext()
        // Todo: Find a better way?
        assert(!getSourcePythonCode(treeCtx, XMLTreeFileName).contains("Error"))
    }
}