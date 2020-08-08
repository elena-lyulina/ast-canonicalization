package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

object Anonymization: Transformation {
    override val metadataKey = "anonymization"
    private val anonymNamesMap: MutableMap<String, String> = mutableMapOf()
    private var numOfVariables = 0
    const val VAR_PREFIX = "v"

    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {
        val tree = treeCtx.root
        val treeIterator = tree.trees.iterator()

        while (treeIterator.hasNext()) {
            val node = treeIterator.next()

            if (treeCtx.getTypeLabel(node) == "NameStore") {
                anonymNamesMap.getOrPut(node.label, { setAnonLabel(node, toStoreMetadata) })
            }

            if (treeCtx.getTypeLabel(node) == "NameLoad") {
                if (node.label in anonymNamesMap.keys) {
                    val newLabel = anonymNamesMap[node.label]
                    if (toStoreMetadata) node.setMetadata(newLabel, node.label)
                    node.label = newLabel
                }

            }
        }

        return
    }

    private fun setAnonLabel(node: ITree, toStoreMetadata: Boolean) : String {
        val newLabel = "$VAR_PREFIX$numOfVariables"
        if (toStoreMetadata) node.setMetadata(newLabel, node.label)
        node.label = newLabel
        numOfVariables += 1
        return newLabel
    }

    override fun reverseApply(treeCtx: TreeContext) {
        val tree = treeCtx.root
        val treeIterator = tree.trees.iterator()

        while (treeIterator.hasNext()) {
            val node = treeIterator.next()
            if (treeCtx.getTypeLabel(node) == "NameStore") {
                node.label = node.getMetadata(node.label) as String
            }
            if (treeCtx.getTypeLabel(node) == "NameLoad" && node.label in anonymNamesMap.values) {
                node.label = node.getMetadata(node.label) as String
            }
        }
    }
}