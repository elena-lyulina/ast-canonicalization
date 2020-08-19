package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

class DummyTransformation: Transformation, NodeTransformer() {
    override val metadataKey: String
        get() = TODO("Not yet implemented")

    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {
        treeCtx.root = visit(treeCtx.root, treeCtx)
    }

    override fun reverseApply(treeCtx: TreeContext) {
        treeCtx.root = reverseVisit(treeCtx.root, treeCtx)
    }

    override fun visit(node: ITree, treeCtx: TreeContext): ITree {
        super.genericVisit(node, treeCtx)
        TODO()
    }

    override fun reverseVisit(node: ITree, treeCtx: TreeContext): ITree {
        super.genericReverseVisit(node, treeCtx)
        TODO()
    }
}