package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.TreeContext

object ConstantOptimizer: Transformation {
    override val metadataKey: String
        get() = TODO("Not yet implemented")

    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {
        val tree = treeCtx.root
        val treeIterator = tree.trees.iterator()

    }

    override fun reverseApply(treeCtx: TreeContext) {
        TODO("Not yet implemented")
    }

}