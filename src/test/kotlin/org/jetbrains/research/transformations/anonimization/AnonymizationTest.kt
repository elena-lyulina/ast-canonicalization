package org.jetbrains.research.transformations.anonimization

import PythonTransformationsTest
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import org.jetbrains.research.transformations.util.ParserSetup
import org.jetbrains.research.transformations.util.Util.getTmpPath
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class AnonymizationTest: PythonTransformationsTest() {

    @Test
    fun checkVariableAnonymizationForTest1() {

        val srcFile = javaClass.getResource("test_1.py").path
        println(srcFile)
        val treeCtx = PythonTreeGenerator().generateFromFile(srcFile)
        Anonymization.apply(treeCtx, true)
        println(getSourcePythonCode(treeCtx, XMLTreeFileName))

    }
}
