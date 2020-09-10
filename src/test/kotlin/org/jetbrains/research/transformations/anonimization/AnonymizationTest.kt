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
//        Anonymization.apply(treeCtx, true)
        println(getSourcePythonCode(treeCtx, "${getTmpPath()}/test.xml"))

    }

//    @Test
//    fun checkReverseApply() {
//        /**
//         * Checking, that reverse transformation brings tests into initial state
//         */
//        val parentDir = "reverseApplyTests/"
//        val codeExamples = arrayListOf<String>("bytes_1.py", "for_1.py", "for_2.py", "simple_break_continue.py",
//        "simple_if.py", "simple_return.py", "big_test.py")
//
//        codeExamples.forEach {
//            val srcFile = javaClass.getResource("${parentDir}${it}").path
//            val treeCtx = PythonTreeGenerator().generateFromFile(srcFile)
//            val originalTreeContext = treeCtx.toString()
//
//            Anonymization.apply(treeCtx, true)
//            Anonymization.inverseApply(treeCtx)
//            assertEquals(originalTreeContext, treeCtx.toString())
//
//        }
//
//    }
}
