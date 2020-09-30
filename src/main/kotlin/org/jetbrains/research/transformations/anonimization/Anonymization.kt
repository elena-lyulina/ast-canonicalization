@file:Suppress("UNCHECKED_CAST")

package org.jetbrains.research.transformations.anonimization

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.transformations.*
import kotlin.reflect.KFunction

object Anonymization : Transformation {
    override val metadataKey = "anonymization"
    private const val VAR_PREFIX = "v"
    private const val FUN_PREFIX = "f"
    private const val ARG_PREFIX = "a"
    private const val CLASS_PREFIX = "c"
    private const val EMPTY_PREFIX = ""
    const val SEPARATOR = "_"
    private val standardLabels = listOf("self", "cls")

    /*
    * Find all places where we should add new counters or reset ids for the top nested levels. On the top nested level,
    * we should add a new counter each time when we define a new function or a new class, in the same time we have to
    * reset all ids (except the first (global) level) before adding a new counter. Thus, we guarantee the correct list
    * of counters for objects from the first (global) level. Also, each time, when the nested level is finished, we
    * should reset all counters.
    * For example, in the following case, we should reset all counters before handling [2], [3], [4], and [5] and add
    * a new counter before handling [2], [4], [5] nodes:
    *
    * [1] a = 5 ...
    * [2] def foo() ...
    * [3] b = 5 ....
    * [4] class ....
    * [5] def foo_2() ...
    * */
    // Todo: can we do it better?
    private fun TreeContext.bordersOfTopNestedLevels(): Pair<List<Int>, List<Int>> {
        val lowerBoundsList = mutableListOf<Int>()
        val upperBoundsList = mutableListOf<Int>()
        val boundNodeTypes = listOf(NodeType.FUNC_DEF.typeLabel, NodeType.CLASS_DEF.typeLabel)
        val firstLevelNodesWithTypes = this.root.children.map { child -> Pair(child.id, this.getTypeLabel(child)) }

        firstLevelNodesWithTypes.mapIndexed { index, (nodeId, nodeType) ->
            if (nodeType in boundNodeTypes) {
                upperBoundsList.add(nodeId)
                firstLevelNodesWithTypes.getOrNull(index + 1)?.let {
                    lowerBoundsList.add(it.first)
                }
            }
        }
        return Pair(lowerBoundsList, upperBoundsList)
    }

    /*
    * Reset all ids (except the first (global) level) for nodes from the [lowerBoundsList]
    * Reset all ids (except the first (global) level) and add a new counter for nodes from the [upperBoundsList]
    * Reset only last id for nodes from the [resetLastIdMap]
    * */
    private fun resetOrSetIds(
        node: ITree, metaInformation: AnonymizationMetaInformation, nodeType: String,
        lowerBoundsList: List<Int>, upperBoundsList: List<Int>,
        nodeIdToRemovingLastItemFunctionsMap: Map<Int, List<KFunction<Unit>>>
    ) {
        if (node.id in lowerBoundsList) {
            metaInformation.resetIds()
        }
        if (node.id in upperBoundsList) {
            metaInformation.resetIds()
            metaInformation.anonVariablesInfo.currentIdsList.add(0)
            metaInformation.anonArgumentsInfo.currentIdsList.add(0)
            if (nodeType == NodeType.CLASS_DEF.typeLabel) {
                metaInformation.anonFunctionsInfo.currentIdsList.add(0)
            }
        }
        nodeIdToRemovingLastItemFunctionsMap[node.id]?.let { removingLastItemFunctions ->
            removingLastItemFunctions.forEach { run { it } }
        }
    }

    /*
    * A function can be nested into a function or into a class
    * */
    private fun isNestedFunctionIntoFunction(prefix: String): Boolean {
        return FUN_PREFIX in prefix.split(SEPARATOR).last { it.isNotEmpty() }
    }

    @ExperimentalStdlibApi
    override fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean) {
        val metaInformation = AnonymizationMetaInformation()

        val (lowerBoundsList, upperBoundsList) = treeCtx.bordersOfTopNestedLevels()
        // This variable stores nodes' ids which indicate that before handling the node we should reset some last ids
        // from the meta-information lists. Each key (id) stores a list of functions to reset ids from the necessary
        // meta-information lists. For example,  if the function is ended we should reset the counters for arguments
        // and for variables (and for functions, if it is a nested function). This map allows us to control the borders
        // of resetting ids.
        // Todo: get a better name
        val nodeIdToRemovingLastItemFunctionsMap = mutableMapOf<Int, List<KFunction<Unit>>>()

        fun anonymizeNode(node: ITree) {
            if (hasStandardLabel(node)) {
                return
            }

            val nodeType = treeCtx.getTypeLabel(node)
            val currentPrefix = getNodeLabelPrefix(node)
            resetOrSetIds(node, metaInformation, nodeType, lowerBoundsList, upperBoundsList, nodeIdToRemovingLastItemFunctionsMap)

            when (nodeType) {
                NodeType.NAME_STORE.typeLabel -> {
                    handleNameStoreNode(node, metaInformation, currentPrefix, toStoreMetadata)
                }
                NodeType.ARG.typeLabel -> {
                    initAndFindLabel(
                        node,
                        metaInformation.anonArgumentsInfo,
                        currentPrefix,
                        toStoreMetadata,
                        NodeType.ARG
                    )
                }
                NodeType.NAME_LOAD.typeLabel -> {
                    handleNameLoadNode(node, metaInformation, currentPrefix, toStoreMetadata)
                }
                NodeType.FUNC_DEF.typeLabel -> {
                    handleFuncOrClassDefinitionNode(
                        node,
                        metaInformation.anonFunctionsInfo,
                        currentPrefix,
                        toStoreMetadata,
                        NodeType.FUNC_DEF,
                        metaInformation,
                        nodeIdToRemovingLastItemFunctionsMap
                    )
                }
                NodeType.CLASS_DEF.typeLabel -> {
                    handleFuncOrClassDefinitionNode(
                        node,
                        metaInformation.anonClassesInfo,
                        currentPrefix,
                        toStoreMetadata,
                        NodeType.CLASS_DEF,
                        metaInformation,
                        nodeIdToRemovingLastItemFunctionsMap
                    )
                }
            }
        }

        treeCtx.transformAndGetNodes(::anonymizeNode)
    }

    private fun hasStandardLabel(node: ITree) = node.label in standardLabels

    private fun findLabel(
        node: ITree, anonymousConfig: AnonymousNamesMap, currentPrefix: String,
        toStoreMetadata: Boolean, toAnonymize: Boolean = true
    ): Boolean {
        val prefixes = anonymousConfig.getPrefixes(node)
        if ((node.label in anonymousConfig.namesMap.keys) && (currentPrefix in prefixes)) {
            if (toAnonymize) {
                anonymizeVariable(node, anonymousConfig, currentPrefix, toStoreMetadata)
            }
            return true
        }
        return false
    }

    private fun initLabel(
        node: ITree, anonymousConfig: AnonymousNamesMap, currentPrefix: String,
        toStoreMetadata: Boolean, nodeType: NodeType
    ) {
        val oldLabel = node.label
        val newLabel = setAnonLabel(
            node, currentPrefix, toStoreMetadata,
            getMarkedLabel(nodeType, anonymousConfig.getLastId())
        )
        anonymousConfig.initLabel(oldLabel, newLabel, currentPrefix)
    }

    private fun initAndFindLabel(
        node: ITree, anonymousConfig: AnonymousNamesMap, currentPrefix: String,
        toStoreMetadata: Boolean, nodeType: NodeType
    ) {
        if (!findLabel(node, anonymousConfig, currentPrefix, toStoreMetadata)) {
            initLabel(node, anonymousConfig, currentPrefix, toStoreMetadata, nodeType)
        }
    }

    private fun getNotEmptyParentLabels(node: ITree) = node.parents.map { it.label }.filter { it.isNotEmpty() }

    private fun isNested(node: ITree, nodeType: NodeType): Boolean {
        val parentFunctionsLabels = getNotEmptyParentLabels(node).filter {
            getNodeLabelPrefix(node).removeSuffix(SEPARATOR) in it
        }
        return parentFunctionsLabels.isNotEmpty()
    }

    /* Check if the node has the parent label and get its name
     * Otherwise return EMPTY_PREFIX
     *
     * The parent label means the function/class and so on where the variable, function or the function argument is defined.
     * We should find the parent label because we want to transform the X variable from the FUN1 into F1_V1
     * not simple V1. If the parent labels list if empty it is the variable from the main block and
     * we should not add a new prefix
     */
    private fun getNodeLabelPrefix(node: ITree): String {
        var prefix = EMPTY_PREFIX
        val parentsLabels = getNotEmptyParentLabels(node)
        if (parentsLabels.isNotEmpty()) {
            prefix = "${parentsLabels[0]}$SEPARATOR"
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
            NodeType.CLASS_DEF -> {
                CLASS_PREFIX
            }
            else -> ""
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
            if (toAddUnderscorePrefix) {
                "${getUnderscorePrefix(node.label)}$prefix$markedLabel"
            } else {
                "$prefix$markedLabel"
            }
        if (toStoreMetadata) node.setMetadata(metadataKey, mutableMapOf(newLabel to node.label))
        node.label = newLabel
        return newLabel
    }

    private fun handleNameStoreNode(
        node: ITree,
        metaInformation: AnonymizationMetaInformation,
        currentPrefix: String,
        toStoreMetadata: Boolean
    ) {
        // Try to find the variable in the arguments of this function
        if (findLabel(node, metaInformation.anonArgumentsInfo, currentPrefix, toStoreMetadata, false)) {
            anonymizeVariable(node, metaInformation.anonArgumentsInfo, currentPrefix, toStoreMetadata)
        } else {
            initAndFindLabel(
                node,
                metaInformation.anonVariablesInfo,
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

    @ExperimentalStdlibApi
    /*
    * See [AnonymizationMetaInformation.updateResetLastIdMapByNestedObjectType]
    * */
    private fun updateResetLastIdMapByNestedObjectType(node: ITree,
                                                       nodeType: NodeType,
                                                       currentPrefix: String,
                                                       metaInformation: AnonymizationMetaInformation,
                                                       nodeIdToRemovingLastItemFunctionsMap: MutableMap<Int, List<KFunction<Unit>>>) {
        if (nodeType == NodeType.CLASS_DEF) {
            metaInformation.updateNodeIdToRemovingLastItemFunctionsMap(node, NestedObjectType.CLASS, nodeIdToRemovingLastItemFunctionsMap)
        } else if (nodeType == NodeType.FUNC_DEF) {
            if (isNestedFunctionIntoFunction(currentPrefix)) {
                metaInformation.updateNodeIdToRemovingLastItemFunctionsMap(
                    node,
                    NestedObjectType.FUNCTION_INTO_CLASS,
                    nodeIdToRemovingLastItemFunctionsMap
                )
            } else {
                metaInformation.updateNodeIdToRemovingLastItemFunctionsMap(
                    node,
                    NestedObjectType.FUNCTION_INTO_FUNCTION,
                    nodeIdToRemovingLastItemFunctionsMap
                )
            }
        }
    }

    @ExperimentalStdlibApi
    private fun handleFuncOrClassDefinitionNode(
        node: ITree,
        currentMetaInformation: AnonymousConfigWithListId,
        currentPrefix: String,
        toStoreMetadata: Boolean,
        nodeType: NodeType,
        fullMetaInformation: AnonymizationMetaInformation,
        nodeIdToRemovingLastItemFunctionsMap: MutableMap<Int, List<KFunction<Unit>>>
    ) {
        if (isNested(node, nodeType)) {
            updateResetLastIdMapByNestedObjectType(node, nodeType, currentPrefix, fullMetaInformation, nodeIdToRemovingLastItemFunctionsMap)
        }
        initLabel(node, currentMetaInformation, currentPrefix, toStoreMetadata, nodeType)
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