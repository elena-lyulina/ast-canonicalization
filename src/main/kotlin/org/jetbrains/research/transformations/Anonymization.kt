@file:Suppress("UNCHECKED_CAST")

package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext


object Anonymization: Transformation {
    override val metadataKey = "anonymization"
    private const val VAR_PREFIX = "v"
    private const val FUN_PREFIX = "f"
    private const val ARG_PREFIX = "a"

    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {

        val anonymVarNamesMap: MutableMap<String, Array<Pair<String, String>>> = mutableMapOf() // х -> array[(new label, parent prefix)]
        val anonymArgNamesMap: MutableMap<String, Array<Pair<String, String>>> = mutableMapOf()
        val anonymFunNamesMap: MutableMap<String, String> = mutableMapOf()

        var numOfVars = 0
        var numOfFuns = 0
        var numOfArgs = 0
        var oldPrefix = ""
        val tree = treeCtx.root
        val treeIterator = tree.trees.iterator()

        while (treeIterator.hasNext()) {
            val node = treeIterator.next()
            val nodeType = treeCtx.getTypeLabel(node)
            val parentFunction = node.parents.map { it.label }.filter { it.isNotEmpty() }
            var prefix = ""
            if (parentFunction.isNotEmpty()) {
                prefix = parentFunction[0] + "_"
            }

            if (oldPrefix != prefix) {
                numOfVars = 0
                numOfArgs = 0
                oldPrefix = prefix
            }

            when (nodeType) {
                NodeType.NAME_STORE.type -> {
                    var prefixes = anonymVarNamesMap[node.label]?.map { it.second }
                    if (prefixes == null) {
                        prefixes = listOf()
                    }

                    if ((node.label in anonymVarNamesMap.keys) and (prefix in prefixes)) {
                        for (pair in anonymVarNamesMap[node.label]!!){
                            if (pair.second == prefix) {
                                val newLabel = pair.first
                                if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
                                node.label = newLabel
                            }
                        }
                    }
                    else {
                        numOfVars += 1
                        val oldLabel = node.label
                        val newLabel = setAnonLabel(node, toStoreMetadata, NodeType.NAME_STORE.type, prefix, numOfVars)
                        anonymVarNamesMap[oldLabel] = arrayOf(Pair(newLabel, prefix))
                    }
                }

                NodeType.NAME_LOAD.type -> {

                    if (node.label in anonymVarNamesMap.keys) {
                        for (pair in anonymVarNamesMap[node.label]!!){
                            if (pair.second == prefix) {
                                val newLabel = pair.first
                                if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
                                node.label = newLabel
                            }
                        }
                    }
                    else if (node.label in anonymFunNamesMap.keys) {
                        val newLabel = anonymFunNamesMap[node.label]
                        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
                        node.label = newLabel
                    }
                    else if (node.label in anonymArgNamesMap.keys) {
                        for (pair in anonymArgNamesMap[node.label]!!){
                            if (pair.second == prefix) {
                                val newLabel = pair.first
                                if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
                                node.label = newLabel
                            }
                        }
                    }

                }

                NodeType.ARG.type -> {

                    var prefixes = anonymArgNamesMap[node.label]?.map { it.second }
                    if (prefixes == null) {
                        prefixes = listOf()
                    }

                    if ((node.label in anonymArgNamesMap.keys) and (prefix in prefixes)) {
                        for (pair in anonymArgNamesMap[node.label]!!){
                            if (pair.second == prefix) {
                                val newLabel = pair.first
                                if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
                                node.label = newLabel
                            }
                        }
                    }
                    else {
                        numOfArgs += 1
                        val oldLabel = node.label
                        val newLabel = setAnonLabel(node, toStoreMetadata, NodeType.ARG.type, prefix, numOfArgs)
                        anonymArgNamesMap[oldLabel] = arrayOf(Pair(newLabel, prefix))
                    }

                }

                NodeType.FUNC_DEF.type -> {
                    anonymFunNamesMap.getOrPut(node.label, {
                        numOfFuns += 1
                        return@getOrPut(setAnonLabel(node, toStoreMetadata, NodeType.FUNC_DEF.type, prefix, numOfFuns))
                    })
                }
            }


        }
    }

    private fun setAnonLabel(node: ITree, toStoreMetadata: Boolean, nodeType: String, prefix: String, num: Int) : String {
        var newLabel = "$num"
        when(nodeType) {
            NodeType.FUNC_DEF.type -> { newLabel = prefix + FUN_PREFIX + newLabel }
            NodeType.NAME_STORE.type -> { newLabel = prefix + VAR_PREFIX + newLabel }
            NodeType.NAME_LOAD.type -> { newLabel = prefix + VAR_PREFIX + newLabel }
            NodeType.ARG.type -> { newLabel = prefix + ARG_PREFIX + newLabel}
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