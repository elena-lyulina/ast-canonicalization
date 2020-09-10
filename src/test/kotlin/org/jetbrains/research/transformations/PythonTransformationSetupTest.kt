package org.jetbrains.research.transformations

import PythonTransformationsTest
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import org.jetbrains.research.transformations.util.Util
import org.junit.jupiter.api.Test

internal class PythonTransformationSetupTest: PythonTransformationsTest() {

    @Test
    fun checkParserSetup() {
        val srcFile = javaClass.getResource("test_1.py").path
        println(srcFile)
        val treeCtx = PythonTreeGenerator().generateFromFile(srcFile)

    }

}