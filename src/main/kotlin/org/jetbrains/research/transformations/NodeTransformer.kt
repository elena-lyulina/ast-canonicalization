package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

abstract class NodeTransformer  {
    protected abstract fun visit(node: ITree, treeCtx: TreeContext) : ITree

    protected abstract fun reverseVisit(node: ITree, treeCtx: TreeContext): ITree

    protected fun genericVisit(node: ITree, treeCtx: TreeContext) {
        node.children = node.children?.map { visit(it, treeCtx) }
    }

    protected fun genericReverseVisit(node: ITree, treeCtx: TreeContext) {
        node.children = node.children?.map { reverseVisit(it, treeCtx) }
    }
}