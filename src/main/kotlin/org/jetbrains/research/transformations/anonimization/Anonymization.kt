@file:Suppress("UNCHECKED_CAST")

package org.jetbrains.research.transformations.anonimization

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.transformations.NodeType
import org.jetbrains.research.transformations.Transformation
import org.jetbrains.research.transformations.transformAndGetNodes

object Anonymization : Transformation {
    override val metadataKey = "anonymization"
    private const val VAR_PREFIX = "v"
    private const val FUN_PREFIX = "f"
    private const val ARG_PREFIX = "a"
    private const val EMPTY_PREFIX = ""
    private const val SEPARATOR = "_"

    /* Check if the node has the parent function and get its name
     * Otherwise return EMPTY_PREFIX
     *
     * The parent function means the function where the variable or the function argument is defined.
     * We should find the parent function because we want to transform the X variable from the FUN1 into F1_V1
     * not simple V1. If the parent functions list if empty it is the variable from the main block and
     * we should not add a new prefix
     */
    private fun getNodeLabelPrefix(node: ITree, oldPrefix: String,
                                   metaInformation: AnonymizationMetaInformation): String {
        var prefix = EMPTY_PREFIX
        val parentFunctions = node.parents.map { it.label }.filter { it.isNotEmpty() }
        if (parentFunctions.isNotEmpty()) {
            prefix = parentFunctions[0] + SEPARATOR
        }
        if (oldPrefix != prefix) {
            /* Reset ids for each new function
               def fun1 (x):
                   x = x + 1
                   return x

                def fun2 (x):
                    x = x + 2
                    return x

              * is transformed into

                 def f1 (f1_a1):
                        f1_v1 = f1_a1 + 1
                        return f1_v1

                 def f2 (f2_a1):
                        f2_v1 = f2_a1 + 2
                        return f2_v1
             */
            metaInformation.anonVariablesMeta.currentId = 0
            metaInformation.anonArgumentsMeta.currentId = 0
        }
        return prefix
    }

    private fun anonymizeVariable(node: ITree,
                                  anonymousConfig: AnonymousConfig,
                                  currentPrefix: String,
                                  toStoreMetadata: Boolean) {
        anonymousConfig.namesMap[node.label]?.forEach { (_, parentPrefix) ->
            if (parentPrefix == currentPrefix) {
                setAnonLabel(node, currentPrefix, toStoreMetadata)
            }
        }
    }

    private fun initOrFindLabel(node: ITree,
                                anonymousConfig: AnonymousConfig,
                                currentPrefix: String,
                                toStoreMetadata: Boolean) {
        val prefixes = anonymousConfig.getPrefixes(node)
        if ((node.label in anonymousConfig.namesMap.keys) && (currentPrefix in prefixes)) {
            anonymizeVariable(node, anonymousConfig, currentPrefix, toStoreMetadata)
        } else {
            val newLabel = setAnonLabel(node, currentPrefix, toStoreMetadata,
                    getMarkedLabel(NodeType.NAME_STORE, anonymousConfig.currentId))
            anonymousConfig.initLabel(node.label, newLabel, currentPrefix)
        }
    }

    private fun handleNameLoadNode(node: ITree,
                                   metaInformation: AnonymizationMetaInformation,
                                   currentPrefix: String,
                                   toStoreMetadata: Boolean) {
        when (node.label) {
            in metaInformation.anonVariablesMeta.namesMap.keys -> {
                anonymizeVariable(node, metaInformation.anonVariablesMeta, currentPrefix, toStoreMetadata)
            }
            in metaInformation.anonFunctionsMeta.namesMap.keys -> {
                val newLabel = metaInformation.anonFunctionsMeta.namesMap[node.label]
                newLabel?.let {
                    setAnonLabel(node, newLabel, toStoreMetadata)
                }
            }
            in metaInformation.anonArgumentsMeta.namesMap.keys -> {
                anonymizeVariable(node, metaInformation.anonArgumentsMeta, currentPrefix, toStoreMetadata)
            }
        }
    }

    private fun handleFuncDefinitionNode(node: ITree,
                                         metaInformation: AnonymizationMetaInformation,
                                         currentPrefix: String,
                                         toStoreMetadata: Boolean) {
        metaInformation.anonFunctionsMeta.namesMap.getOrPut(node.label, {
            val newLabel = setAnonLabel(node, currentPrefix, toStoreMetadata,
                    getMarkedLabel(NodeType.FUNC_DEF, metaInformation.anonFunctionsMeta.currentId))
            metaInformation.anonFunctionsMeta.currentId += 1
            newLabel
        })

    }

    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {
        val metaInformation = AnonymizationMetaInformation()
        var oldPrefix = EMPTY_PREFIX

        fun anonymizeNode(node: ITree) {
            val nodeType = treeCtx.getTypeLabel(node)
            val currentPrefix = getNodeLabelPrefix(node, oldPrefix, metaInformation)
            oldPrefix = currentPrefix

            when (nodeType) {
                NodeType.NAME_STORE.type -> {
                    initOrFindLabel(node, metaInformation.anonVariablesMeta, currentPrefix, toStoreMetadata)
                }
                NodeType.NAME_LOAD.type -> {
                    handleNameLoadNode(node, metaInformation, currentPrefix, toStoreMetadata)
                }
                NodeType.ARG.type -> {
                    initOrFindLabel(node, metaInformation.anonArgumentsMeta, currentPrefix, toStoreMetadata)
                }
                NodeType.FUNC_DEF.type -> {
                    handleFuncDefinitionNode(node, metaInformation, currentPrefix, toStoreMetadata)
                }
            }
        }

        treeCtx.transformAndGetNodes(::anonymizeNode)
    }

    private fun getPrefixByNodeType(nodeType: NodeType): String {
        return when (nodeType) {
            NodeType.FUNC_DEF -> {
                FUN_PREFIX
            }
            NodeType.NAME_STORE -> {
                VAR_PREFIX
            }
            NodeType.NAME_LOAD -> {
                VAR_PREFIX
            }
            NodeType.ARG -> {
                ARG_PREFIX
            }
            else -> EMPTY_PREFIX
        }
    }

    private fun getMarkedLabel(nodeType: NodeType, currentId: Int): String {
        val prefix = getPrefixByNodeType(nodeType)
        return prefix + currentId
    }

    private fun setAnonLabel(node: ITree, prefix: String, toStoreMetadata: Boolean,
                             markedLabel: String = EMPTY_PREFIX): String {
        val newLabel = prefix + markedLabel
        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
        node.label = newLabel
        return newLabel
    }

    override fun inverseApply(treeCtx: TreeContext) {
        treeCtx.transformAndGetNodes(::restoreLabelName)
    }

    private fun restoreLabelName(node: ITree) {
        node.getMetadata(metadataKey)?.let {
            val nodeMap = it as? MutableMap<String, String>
                    ?: throw IllegalArgumentException("Value with key $metadataKey should be a map")
            node.label = nodeMap[node.label]
        }
    }
}