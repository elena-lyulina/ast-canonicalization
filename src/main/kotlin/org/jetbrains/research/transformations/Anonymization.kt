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

        val anonymVarNamesMap: MutableMap<String, String> = mutableMapOf()
        val anonymArgNamesMap: MutableMap<String, String> = mutableMapOf()
        val anonymFunNamesMap: MutableMap<String, String> = mutableMapOf()

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

                    if (anonymVarNamesMap[node.label] != null) {
                        val newLabel = anonymVarNamesMap[node.label]
                        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
                        node.label = newLabel
                    }

                    anonymVarNamesMap.getOrPut(node.label, {
                        numOfVariables += 1
                        return@getOrPut(setAnonLabel(node, toStoreMetadata, NodeType.NAME_STORE.type, numOfVariables))
                    })
                }

                NodeType.NAME_LOAD.type -> {
                    val parentsTypes = node.parents.map { it.label }
//                    print("${anonymFunNamesMap.values} & $parentsTypes")
//                    print(anonymFunNamesMap.values.intersect(parentsTypes))

                    if (anonymFunNamesMap.values.intersect(parentsTypes).isNotEmpty()) {
                        val newLabel = anonymArgNamesMap[node.label]
                        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
                        node.label = newLabel
                    }
                    else if (node.label in anonymVarNamesMap.keys) {
                        val newLabel = anonymVarNamesMap[node.label]
                        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
                        node.label = newLabel
                    }
                }

                NodeType.ARG.type -> {
                    anonymArgNamesMap.getOrPut(node.label, {
                        numOfVariables += 1
                        return@getOrPut(setAnonLabel(node, toStoreMetadata, NodeType.ARG.type, numOfVariables))
                    })
                }
                // end of variable anonymization

                //function anonymization
                NodeType.FUNC_DEF.type -> {
                    anonymFunNamesMap.getOrPut(node.label, {
                        numOfFunctions += 1
                        return@getOrPut(setAnonLabel(node, toStoreMetadata, NodeType.FUNC_DEF.type, numOfFunctions))
                    })
                }
            }




        }
    }

    private fun setAnonLabel(node: ITree, toStoreMetadata: Boolean, nodeType: String, num: Int) : String {
        var newLabel = "$num"
        when(nodeType) {
            NodeType.FUNC_DEF.type -> { newLabel = FUN_PREFIX + newLabel }
            NodeType.NAME_STORE.type -> { newLabel = VAR_PREFIX + newLabel }
            NodeType.NAME_LOAD.type -> { newLabel = VAR_PREFIX + newLabel }
            NodeType.ARG.type -> { newLabel = VAR_PREFIX + newLabel}
        }

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