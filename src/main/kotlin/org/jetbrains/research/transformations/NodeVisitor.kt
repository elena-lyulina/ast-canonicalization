package org.jetbrains.research.transformations
import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

interface NodeVisitor {

    fun visit(node: ITree, treeCtx: TreeContext) : ITree

    fun reverseVisit(node: ITree, treeCtx: TreeContext) : ITree

    fun genericVisit(node: ITree, treeCtx: TreeContext)

    fun genericReverseVisit(node: ITree, treeCtx: TreeContext)

}