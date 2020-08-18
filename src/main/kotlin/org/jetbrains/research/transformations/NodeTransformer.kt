package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

open class NodeTransformer : NodeVisitor {
    override fun visit(node: ITree, treeCtx: TreeContext) : ITree {
        return node
    }

    override fun reverseVisit(node: ITree, treeCtx: TreeContext): ITree {
        return node
    }

    override fun genericVisit(node: ITree, treeCtx: TreeContext) {
        node.children = node.children?.map { visit(it, treeCtx) }
    }

    override fun genericReverseVisit(node: ITree, treeCtx: TreeContext) {
        node.children = node.children?.map { reverseVisit(it, treeCtx) }
    }
}