package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

class ConstantOptimizer(treeCtx: TreeContext): RecursiveTransformation() {

    private val treeExplorer: TreeExplorer = TreeExplorer(treeCtx)

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

        if (!treeExplorer.isBinOp(node)) {
            return node
        }

        val opName = treeExplorer.getOperationName(node)
        val left = node.getChild(0)
        val right = node.getChild(1)

        when {
            treeExplorer.isConstLikeType(left) and treeExplorer.isConstLikeType(right) -> {
                val opResult: OperationResult? = treeExplorer.getBinOpResult(opName, left, right)
                val resTypeLabel = treeExplorer.getConstLikeTypeName(left) + "-" + opResult!!.resultPythonTypeName
                val resTypeHash = resTypeLabel.hashCode()

                return treeCtx.createTree(resTypeHash, opResult.result.toString(), resTypeLabel)
            }

//            treeExplorer.isConstLikeType(left) -> {
//
//                if (treeExplorer.getOperationName(node) in listOf("Mult", "Pow")) {
//                    TODO()
//                }
//            }
//
//            treeExplorer.isConstLikeType(right) -> {
//                TODO()
//            }
            else -> return node
        }
    }

    override fun inverseVisit(node: ITree, treeCtx: TreeContext): ITree {
        TODO("Not yet implemented")
    }
}
