@file:Suppress("UNCHECKED_CAST")

package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.Tree
import com.github.gumtreediff.tree.TreeContext

enum class NodeType(val type: String) {
    NAME_STORE("NameStore"),
    NAME_LOAD("NameLoad")
}

object Anonymization: Transformation {
    override val metadataKey = "anonymization"
    private const val VAR_PREFIX = "v"


    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {
        val anonymNamesMap: MutableMap<String, String> = mutableMapOf()
        var numOfVariables = 0
        val tree = treeCtx.root
        val treeIterator = tree.trees.iterator()

        while (treeIterator.hasNext()) {
            val node = treeIterator.next()

            val nodeType = treeCtx.getTypeLabel(node)
            when (nodeType) {
                NodeType.NAME_STORE.type -> {
                    anonymNamesMap.getOrPut(node.label, {
                        numOfVariables += 1
                        return@getOrPut(setAnonLabel(node, toStoreMetadata, numOfVariables))
                    })
                }

                NodeType.NAME_LOAD.type -> {
                    if (node.label in anonymNamesMap.keys) {
                        val newLabel = anonymNamesMap[node.label]
                        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
                        node.label = newLabel
                    }
                }
                else -> {
                    // default actions, for example log a message
                }
            }


        }
    }

    private fun setAnonLabel(node: ITree, toStoreMetadata: Boolean, numOfVariables: Int) : String {
        val newLabel = "$VAR_PREFIX$numOfVariables"
        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
        node.label = newLabel
        return newLabel
    }

    override fun inverseApply(treeCtx: TreeContext) {
        val tree = treeCtx.root
        val treeIterator = tree.trees.iterator()

        while (treeIterator.hasNext()) {
            val node = treeIterator.next()
            if (treeCtx.getTypeLabel(node) == "NameStore") {
                restoreLabelName(node)
            }

            if (treeCtx.getTypeLabel(node) == "NameLoad") {
                restoreLabelName(node)
            }
        }
    }

    private fun restoreLabelName(node: ITree) {
        node.getMetadata(metadataKey)?.let {
            val nodeMap = it as? MutableMap<String, String> ?:
            throw IllegalArgumentException("Value with key $metadataKey should be a map")
            node.label = nodeMap[node.label]
        }
    }
}