package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext


/*
* Common base class for all tree transformations, that use recursion
* transformation pattern
* */
abstract class RecursiveTransformation: Transformation {

    /**
     * Forward transformation of the node performed recursively:
     * first transforming the children of the node, then the node itself
     * @param [node] - node from AST to apply the transformation to.
     * @param [treeCtx] - full AST context containing the necessary additional data.
     */
    protected abstract fun visit(node: ITree, treeCtx: TreeContext) : ITree

    /**
     * Inverse transformation of the node performed recursively:
     * first transforming the children of the node, then the node itself
     * @param [node] - node from AST to apply the transformation to.
     * @param [treeCtx] - full AST context containing the necessary additional data.
     */
    protected abstract fun inverseVisit(node: ITree, treeCtx: TreeContext) : ITree

    /**
     * Updating children of the node using `visit` function
     * when performing forward transformation
     * @param [node] - node of the AST whose children will be updated.
     * @param [treeCtx] - full AST context containing the necessary additional data.
     */
    protected fun visitChildren(node: ITree, treeCtx: TreeContext) {
        node.children = node.children?.map { visit(it, treeCtx) }
    }

    /**
     * Updating children of the node using `inverseVisit` function
     * when performing inverse transformation
     * @param [node] - node of the AST whose children will be updated.
     * @param [treeCtx] - full AST context containing the necessary additional data.
     */
    protected fun inverseVisitChildren(node: ITree, treeCtx: TreeContext) {
        node.children = node.children?.map { inverseVisit(it, treeCtx) }
    }
}