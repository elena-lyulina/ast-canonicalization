package org.jetbrains.research.transformations.anonimization

import PythonTransformationsTest
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.transformations.util.Util.getContentFromFile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream


internal class AnonymizationTest : PythonTransformationsTest() {

    companion object {
        @JvmStatic
        fun getResourceFolder(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    getResourcePath("in_1.py", ::AnonymizationTest),
                    getResourcePath("out_1.py", ::AnonymizationTest)
                )
            )
        }
    }

    @ParameterizedTest(name = "Apply anonymization test")
    @MethodSource("getResourceFolder")
    fun applyAnonymizationTests(inFile: File, outFile: File) {
        transformCode(inFile, outFile, Anonymization::apply)
    }

    @ParameterizedTest(name = "Inverse apply anonymization test")
    @MethodSource("getResourceFolder")
    fun inverseApplyAnonymizationTests(outFile: File, inFile: File) {
        transformCode(inFile, outFile) { ctx: TreeContext, _: Boolean ->
            (Anonymization::apply)(ctx, true)
            (Anonymization::inverseApply)(ctx)
        }
    }

}
