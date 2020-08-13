package org.jetbrains.research.transformations

import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.transformations.util.ParserSetup
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AnonymizationTest {

    @Test
    fun checkVariableAnonymizationForTest_1() {
        ParserSetup.checkSetup()

        val srcFile = javaClass.getResource("test_1.py").path
        val treeCtx : TreeContext = PythonTreeGenerator().generateFromFile(srcFile)

        Anonymization.apply(treeCtx, true)
        val tree = treeCtx.root
        val treeIterator = tree.trees.iterator()


        while (treeIterator.hasNext()) {
            val node = treeIterator.next()
            if (treeCtx.getTypeLabel(node) == "NameStore") {
                val nodeMap = node.getMetadata(Anonymization.metadataKey) as MutableMap<String, String>
                if (nodeMap.containsKey("v0")) {
                    assertEquals("a", nodeMap["v0"])
                }
                if (nodeMap.containsKey("v1")) {
                    assertEquals("b", nodeMap["v1"])
                }
                if (nodeMap.containsKey("v2")) {
                    assertEquals("c", nodeMap["v2"])
                }
            }
        }
    }

    @Test
    fun checkReverseApply() {
        /**
         * Checking, that reverse transformation brings tests into initial state
         */
        ParserSetup.checkSetup()
        val parentDir = "reverseApplyTests/"
        val codeExamples = arrayListOf<String>("bytes_1.py", "for_1.py", "for_2.py", "simple_break_continue.py",
        "simple_if.py", "simple_return.py", "big_test.py")

        codeExamples.forEach {
            val srcFile = javaClass.getResource("${parentDir}${it}").path
            val treeCtx: TreeContext = PythonTreeGenerator().generateFromFile(srcFile)
            val originalTreeContext = treeCtx.toString()

            Anonymization.apply(treeCtx, true)
            Anonymization.reverseApply(treeCtx)
            assertEquals(originalTreeContext, treeCtx.toString())

        }

    }
}