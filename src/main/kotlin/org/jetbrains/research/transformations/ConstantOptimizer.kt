package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

object ConstantOptimizer: RecursiveTransformation() {
    override val metadataKey: String
        get() = TODO("Not yet implemented")


    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {
        treeCtx.root = visit(treeCtx.root, treeCtx)
    }

    override fun inverseApply(treeCtx: TreeContext) {
        treeCtx.root = inverseVisit(treeCtx.root, treeCtx)
    }

    override fun visit(node: ITree, treeCtx: TreeContext): ITree {
        super.visitChildren(node, treeCtx)

        if (treeCtx.getTypeLabel(node) != "BinOp_Add")
            return node

        val left = node.getChild(0)
        val right = node.getChild(1)
        val typeLabel: String = treeCtx.getTypeLabel(left)
        val newVal = left.label.toInt() + right.label.toInt()

        return treeCtx.createTree(left.type, newVal.toString(), typeLabel)

    }

    override fun inverseVisit(node: ITree, treeCtx: TreeContext): ITree {
        TODO("Not yet implemented")
    }
}