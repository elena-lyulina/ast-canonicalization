package org.jetbrains.research.transformations.anonimization

import com.github.gumtreediff.tree.ITree
import kotlin.reflect.KFunction

interface Identifiable {
    fun getLastId(): Int

    fun resetIds()

    fun removeLast()
}

/*
* We need to have different ids counters for the different nested levels. The first item of the list is always used for the global counters. Consider the following example:

* a = 5
* def foo(b):
*     b = 5
*     pass
*
* We should have a counter for the `a` variable and another one for the `b` variable because we want to reset ids in the each nested level.
* */
interface ListIdCounter : Identifiable {
    var currentIdsList: MutableList<Int>

    /*
    * Get the id for the last nested level
    * */
    override fun getLastId(): Int {
        return currentIdsList.last()
    }

    /*
    * Reset all ids except for the first (global) level
    * */
    override fun resetIds() {
        currentIdsList = mutableListOf(currentIdsList.first())
    }

    /*
    * Reset the id for the last nested level
    * */
    @ExperimentalStdlibApi
    override fun removeLast() {
        currentIdsList.removeLast()
    }
}

typealias NamesMap = MutableMap<String, List<Pair<String, String>>>

/*
* @property namesMap stores a hashmap for labels for the different nodes. The format of it is:
* OLD_LABEL -> list[(NEW_LABEL, FUNCTION_PREFIX)]
* This kind of the namesMap is used for the variables and the functions arguments.
* We can not store only one NEW_LABEL because for each variable with the same name, which is used in the different places
* (in the different functions or in the main block) we have to define which name is valid for the current situation.
* For example, if the variable X is defined in the functions A and B then we have to store in the namesMap
* FUNCTION_PREFIXes for both of them to set the new name unequivocally by the current function name
* */
abstract class AnonymousNamesMap(open val namesMap: NamesMap) : Identifiable {
    abstract fun initLabel(oldLabel: String, newLabel: String, parentPrefix: String)

    fun getPrefixes(node: ITree): List<String> {
        return namesMap[node.label]?.map { it.second } ?: emptyList()
    }

    fun getNewLabelByPrefix(oldLabel: String, prefix: String): String? {
        return namesMap[oldLabel]?.find { it.second == prefix }?.first
    }

    fun getNewLabelByLabel(oldLabel: String, prefix: String, separator: String = Anonymization.SEPARATOR): String? {
        return namesMap[oldLabel]?.find { it.first == prefix.removeSuffix(separator) }?.first
    }
}

data class AnonymousConfigWithListId(
    override var currentIdsList: MutableList<Int> = mutableListOf(0),
    override val namesMap: MutableMap<String, List<Pair<String, String>>> = mutableMapOf()
) : ListIdCounter, AnonymousNamesMap(namesMap) {

    /*
    * Add a new item into [namesMap] and increment the id from the last (current) nested level
    * */
    override fun initLabel(oldLabel: String, newLabel: String, parentPrefix: String) {
        namesMap[oldLabel] = listOf(Pair(newLabel, parentPrefix))
        currentIdsList[currentIdsList.lastIndex]++
    }
}

data class AnonymizationMetaInformation(
    val anonArgumentsInfo: AnonymousConfigWithListId = AnonymousConfigWithListId(),
    val anonVariablesInfo: AnonymousConfigWithListId = AnonymousConfigWithListId(),
    val anonFunctionsInfo: AnonymousConfigWithListId = AnonymousConfigWithListId(),
    val anonClassesInfo: AnonymousConfigWithListId = AnonymousConfigWithListId()
) {

    fun resetIds() {
        anonVariablesInfo.resetIds()
        anonArgumentsInfo.resetIds()
        anonFunctionsInfo.resetIds()
        anonClassesInfo.resetIds()
    }

    fun findLabel(label: String, prefix: String): String? {
        // Try to find the current variable
        anonArgumentsInfo.getNewLabelByPrefix(label, prefix)?.let { return it }
        anonVariablesInfo.getNewLabelByPrefix(label, prefix)?.let { return it }
        anonFunctionsInfo.getNewLabelByPrefix(label, prefix)?.let { return it }
        anonClassesInfo.getNewLabelByPrefix(label, prefix)?.let { return it }

        /*
        * Try to find the current variable into names. For example if we have the function:
        *
        * def foo():
        *    print(foo)
        *    print(foo())
        *
        * We have to find foo in the names of anonFunctionsMeta (or in the anonClassesMeta if it is a class)
        */
        anonFunctionsInfo.getNewLabelByLabel(label, prefix)?.let { return it }
        anonClassesInfo.getNewLabelByLabel(label, prefix)?.let { return it }
        return null
    }

    // Todo: thinking about names
    /*
    * See [NestedObjectType]
    * At different nesting levels, we want to update the counters differently after the current nesting level ends.
    * For example, if the external function is ended, then we want to reset only the counters of variables and arguments.
    * But if the function was nested, then we also want to reset the counter for the functions in the meta-information.
    * We get the root of the subtree (the beginning of the nesting level) and the [NestedObjectType] that characterizes
    * the nesting level of the current vertex. Following this type, we can determine which of the counters we want to
    * reset after the end of the current nesting level and update the [nodeIdToRemovingLastItemFunctionsMap].
    * */
    @ExperimentalStdlibApi
    fun updateNodeIdToRemovingLastItemFunctionsMap(node: ITree, nestedObjectType: NestedObjectType,
                                                   nodeIdToRemovingLastItemFunctionsMap: MutableMap<Int, List<KFunction<Unit>>>) {
        val levels = listOf(anonVariablesInfo, anonArgumentsInfo, anonFunctionsInfo, anonClassesInfo)
        // This list stored functions that allowing remove the last item from a meta-information list. Sometimes we
        // want to remove the last item from several meta-information lists, but not from all. So we stores which
        // meta-information lists will be used for deleting the last item.
        val removingLastItemFunctions = mutableListOf<KFunction<Unit>>()
        levels.subList(0, nestedObjectType.nestedLevel).forEach{
            it.currentIdsList.add(0)
            removingLastItemFunctions.add(it::removeLast)
        }
        nodeIdToRemovingLastItemFunctionsMap[node.children.last().id] = removingLastItemFunctions
    }
}

/*
* We have 4 levels to reset (or add) ids:
* - [1] variables
* - [2] arguments
* - [3] functions
* - [4] classes
* For each nested object type we should reset ids on the different levels
* If a nested object is a function nested into another function, we should use only [1] and [2] levels.
* If a nested object is a function nested into a class, we should use [1], [2] and [3] levels.
* If a nested object is a class, we should use all levels.
* */
enum class NestedObjectType(val nestedLevel: Int) {
    CLASS(4),
    FUNCTION_INTO_CLASS(3),
    FUNCTION_INTO_FUNCTION(2)
}