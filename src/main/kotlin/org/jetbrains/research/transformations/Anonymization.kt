@file:Suppress("UNCHECKED_CAST")

package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

enum class NodeType(val type: String) {
    NAME_STORE("Name_Store"),
    NAME_LOAD("Name_Load"),
    ARG("arg"),
    FUNC_DEF("FunctionDef")
}

object Anonymization: Transformation {
    override val metadataKey = "anonymization"
    private const val VAR_PREFIX = "v"
    private const val FUN_PREFIX = "f"


    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {

        val anonymNamesMap: MutableMap<String, String> = mutableMapOf()
        var numOfVariables = 0
        var numOfFunctions = 0
        val tree = treeCtx.root
        val treeIterator = tree.trees.iterator()

        while (treeIterator.hasNext()) {
            val node = treeIterator.next()
            val nodeType = treeCtx.getTypeLabel(node)

            when (nodeType) {
                // variable anonymization
                NodeType.NAME_STORE.type -> {
                    anonymNamesMap.getOrPut(node.label, {
                        numOfVariables += 1
                        return@getOrPut(setAnonLabel(node, toStoreMetadata, NodeType.NAME_STORE.type, numOfVariables))
                    })
                }

                NodeType.NAME_LOAD.type -> {
                    if (node.label in anonymNamesMap.keys) {
                        val newLabel = anonymNamesMap[node.label]
                        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
                        node.label = newLabel
                    }
                }

                NodeType.ARG.type -> {
                    anonymNamesMap.getOrPut(node.label, {
                        numOfVariables += 1
                        return@getOrPut(setAnonLabel(node, toStoreMetadata, NodeType.ARG.type, numOfVariables))
                    })
                }
                // end of variable anonymization

                //function anonymization
                NodeType.FUNC_DEF.type -> {
                    anonymNamesMap.getOrPut(node.label, {
                        numOfFunctions += 1
                        return@getOrPut(setAnonLabel(node, toStoreMetadata, NodeType.FUNC_DEF.type, numOfFunctions))
                    })
                }
            }




        }
    }

    private fun setAnonLabel(node: ITree, toStoreMetadata: Boolean, type: String, num: Int) : String {
        var newLabel = "$num"
        when(type) {
            NodeType.FUNC_DEF.type -> { newLabel = FUN_PREFIX + newLabel }
            NodeType.NAME_STORE.type -> { newLabel = VAR_PREFIX + newLabel }
            NodeType.NAME_LOAD.type -> { newLabel = VAR_PREFIX + newLabel }
            NodeType.ARG.type -> { newLabel = VAR_PREFIX + newLabel}
        }
//        newLabel = "$VAR_PREFIX$num"
        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
        node.label = newLabel
        return newLabel
    }

    override fun inverseApply(treeCtx: TreeContext) {
        val tree = treeCtx.root
        val treeIterator = tree.trees.iterator()

        while (treeIterator.hasNext()) {
            val node = treeIterator.next()
            val nodeType = treeCtx.getTypeLabel(node)

            when (nodeType) {
                NodeType.NAME_STORE.type -> { restoreLabelName(node) }
                NodeType.NAME_LOAD.type -> { restoreLabelName(node) }
                NodeType.ARG.type -> { restoreLabelName(node)}
                NodeType.FUNC_DEF.type -> { restoreLabelName(node)}
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