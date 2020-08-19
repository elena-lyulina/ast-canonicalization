package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

class DummyTransformation: BasicTransformation() {
    override fun visit(node: ITree, treeCtx: TreeContext): ITree {
        super.genericVisit(node, treeCtx)
        TODO()
    }

    override fun reverseVisit(node: ITree, treeCtx: TreeContext): ITree {
        super.genericReverseVisit(node, treeCtx)
        TODO()
    }

    override val metadataKey: String
        get() = TODO("Not yet implemented")

    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {
        TODO("Not yet implemented")
    }

    override fun reverseApply(treeCtx: TreeContext) {
        TODO("Not yet implemented")
    }
}