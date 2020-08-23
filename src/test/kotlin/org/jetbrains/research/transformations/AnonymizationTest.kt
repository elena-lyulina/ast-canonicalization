package org.jetbrains.research.transformations

import ParserSetupForTests
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.tree.TreeContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test


internal class AnonymizationTest: ParserSetupForTests() {

    @Test
    fun checkVariableAnonymizationForTest1() {

        val srcFile = javaClass.getResource("test_1.py").path
        println(srcFile)
        val treeCtx = PythonTreeGenerator().generateFromFile(srcFile)

        Anonymization.apply(treeCtx, true)
        val tree = treeCtx.root
        val treeIterator = tree.trees.iterator()


        while (treeIterator.hasNext()) {
            val node = treeIterator.next()
            if (treeCtx.getTypeLabel(node) == "NameStore") {
                val nodeMap = node.getMetadata(Anonymization.metadataKey) as MutableMap<String, String>
                if (nodeMap.containsKey("v1")) {
                    assertEquals("a", nodeMap["v1"])
                }
                if (nodeMap.containsKey("v2")) {
                    assertEquals("b", nodeMap["v2"])
                }
                if (nodeMap.containsKey("v3")) {
                    assertEquals("c", nodeMap["v3"])
                }
            }
        }
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