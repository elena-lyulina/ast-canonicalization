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
    const val SEPARATOR = "_"

    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {
        val metaInformation = AnonymizationMetaInformation()
        var oldPrefix = EMPTY_PREFIX

        fun anonymizeNode(node: ITree) {
            val nodeType = treeCtx.getTypeLabel(node)
            val currentPrefix = getNodeLabelPrefix(node, oldPrefix, metaInformation)
            oldPrefix = currentPrefix

            when (nodeType) {
                NodeType.NAME_STORE.type -> {
                    handleNameStoreNode(node, metaInformation, currentPrefix, toStoreMetadata)
                }
                NodeType.ARG.type -> {
                    handleArgNode(node, metaInformation, currentPrefix, toStoreMetadata)
                }
                NodeType.NAME_LOAD.type -> {
                    handleNameLoadNode(node, metaInformation, currentPrefix, toStoreMetadata)
                }
                NodeType.FUNC_DEF.type -> {
                    handleFuncDefinitionNode(node, metaInformation, currentPrefix, toStoreMetadata)
                }
            }
        }

        treeCtx.transformAndGetNodes(::anonymizeNode)
    }

    private fun findLabel(node: ITree, anonymousConfig: AnonymousNamesMap, currentPrefix: String,
                          toStoreMetadata: Boolean, toAnonymize: Boolean = true): Boolean {
        val prefixes = anonymousConfig.getPrefixes(node)
        if ((node.label in anonymousConfig.namesMap.keys) && (currentPrefix in prefixes)) {
            if (toAnonymize) {
                anonymizeVariable(node, anonymousConfig, currentPrefix, toStoreMetadata)
            }
            return true
        }
        return false
    }

    private fun initLabel(node: ITree, anonymousConfig: AnonymousNamesMap, currentPrefix: String,
        toStoreMetadata: Boolean, nodeType: NodeType) {
        val oldLabel = node.label
        val newLabel = setAnonLabel(
            node, currentPrefix, toStoreMetadata,
            getMarkedLabel(nodeType, anonymousConfig.getLastId())
        )
        anonymousConfig.initLabel(oldLabel, newLabel, currentPrefix)
    }

    private fun initAndFindLabel(node: ITree, anonymousConfig: AnonymousNamesMap, currentPrefix: String,
        toStoreMetadata: Boolean, nodeType: NodeType) {
        if (!findLabel(node, anonymousConfig, currentPrefix, toStoreMetadata)) {
            initLabel(node, anonymousConfig, currentPrefix, toStoreMetadata, nodeType)
        }
    }

    private fun getNotEmptyParentLabels(node: ITree) = node.parents.map { it.label }.filter { it.isNotEmpty() }

    private fun isNested(node: ITree, nodeType: NodeType): Boolean {
        val parentFunctionsLabels = getNotEmptyParentLabels(node).filter { it.contains(getPrefixByNodeType(nodeType)) }
        return parentFunctionsLabels.isNotEmpty()
    }

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
        val parentsLabels = getNotEmptyParentLabels(node)
        if (parentsLabels.isNotEmpty()) {
            prefix = "${parentsLabels[0]}$SEPARATOR"
        }
        // Reset ids for each new function/class and etc
        if (oldPrefix != prefix) {
            metaInformation.resetIds()
        }
        return prefix
    }

    private fun anonymizeVariable(
        node: ITree, anonymousConfig: AnonymousNamesMap, currentPrefix: String,
        toStoreMetadata: Boolean
    ) {
        anonymousConfig.getNewLabelByPrefix(node.label, currentPrefix)?.let {
            setAnonLabel(node, it, toStoreMetadata)
        }
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

    private fun getUnderscorePrefix(label: String): String {
        val underscoreRegEx = Regex("^(_+)(.+)")
        if (underscoreRegEx.containsMatchIn(label)) {
            val match = underscoreRegEx.find(label)!!
            val (underscore, _) = match.destructured
            return underscore
        }
        return ""
    }

    private fun setAnonLabel(
        node: ITree, prefix: String, toStoreMetadata: Boolean,
        markedLabel: String = EMPTY_PREFIX, toAddUnderscorePrefix: Boolean = true
    ): String {
        val newLabel =
            if (toAddUnderscorePrefix) "${getUnderscorePrefix(node.label)}$prefix$markedLabel" else "$prefix$markedLabel"
        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
        node.label = newLabel
        return newLabel
    }

    private fun handleArgNode(
        node: ITree,
        metaInformation: AnonymizationMetaInformation,
        currentPrefix: String,
        toStoreMetadata: Boolean
    ) {
        initAndFindLabel(node, metaInformation.anonArgumentsMeta, currentPrefix, toStoreMetadata, NodeType.ARG)
    }

    private fun handleNameStoreNode(
        node: ITree,
        metaInformation: AnonymizationMetaInformation,
        currentPrefix: String,
        toStoreMetadata: Boolean
    ) {
        // Try to find the variable in the arguments of this function
        if (findLabel(node, metaInformation.anonArgumentsMeta, currentPrefix, toStoreMetadata, false)) {
            anonymizeVariable(node, metaInformation.anonArgumentsMeta, currentPrefix, toStoreMetadata)
        } else {
            initAndFindLabel(
                node,
                metaInformation.anonVariablesMeta,
                currentPrefix,
                toStoreMetadata,
                NodeType.NAME_STORE
            )
        }
    }

    private fun handleNameLoadNode(
        node: ITree,
        metaInformation: AnonymizationMetaInformation,
        currentPrefix: String,
        toStoreMetadata: Boolean
    ) {
        metaInformation.findLabel(node.label, currentPrefix)?.let {
            setAnonLabel(node, it, toStoreMetadata, toAddUnderscorePrefix = false)
        }
    }

    private fun handleFuncDefinitionNode(
        node: ITree,
        metaInformation: AnonymizationMetaInformation,
        currentPrefix: String,
        toStoreMetadata: Boolean
    ) {
        if (isNested(node, NodeType.FUNC_DEF)) {
            metaInformation.anonFunctionsMeta.currentIdsList.add(0)
        }
        initLabel(node, metaInformation.anonFunctionsMeta, currentPrefix, toStoreMetadata, NodeType.FUNC_DEF)
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