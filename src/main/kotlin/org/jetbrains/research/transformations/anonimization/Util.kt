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

/*
* @property namesMap stores a hashmap for labels for the different nodes. The format of it is:
* OLD_LABEL -> list[(NEW_LABEL, FUNCTION_PREFIX)]
* This kind of the namesMap is used for the variables and the functions arguments.
* We can not store only one NEW_LABEL because for each variable with the same name, which is used in the different places
* (in the different functions or in the main block) we have to define which name is valid for the current situation.
* For example, if the variable X is defined in the functions A and B then we have to store in the namesMap
* FUNCTION_PREFIXes for both of them to set the new name unequivocally by the current function name
* */
abstract class AnonymousNamesMap(open val namesMap: MutableMap<String, List<Pair<String, String>>>) : Identifiable {
    abstract fun initLabel(oldLabel: String, newLabel: String, parentPrefix: String)

    fun getPrefixes(node: ITree): List<String> {
        return namesMap.getOrDefault(node.label, listOf()).map { (_, parent_prefix) -> parent_prefix }
    }

    fun getNewLabelByPrefix(oldLabel: String, prefix: String): String? {
        return namesMap[oldLabel]?.find { (_, parentPrefix) -> parentPrefix == prefix }
            ?.let { (newLabel, _) ->
                newLabel
            }
    }

    fun getNewLabelByLabel(oldLabel: String, prefix: String, separator: String = Anonymization.SEPARATOR): String? {
        return namesMap[oldLabel]?.find { (newLabel, _) -> newLabel == prefix.removeSuffix(separator) }
            ?.let { (newLabel, _) ->
                newLabel
            }
    }
}

data class AnonymousConfigWithListId(
    override var currentIdsList: MutableList<Int> = mutableListOf(0),
    override val namesMap: MutableMap<String, List<Pair<String, String>>> = mutableMapOf()
) : ListIdCounter, AnonymousNamesMap(namesMap) {

    /*
    * Add a new item into [namesMap] and increment the if from the last (current) nested level
    * */
    override fun initLabel(oldLabel: String, newLabel: String, parentPrefix: String) {
        namesMap[oldLabel] = listOf(Pair(newLabel, parentPrefix))
        currentIdsList[currentIdsList.size - 1]++
    }
}

data class AnonymizationMetaInformation(
    val anonArgumentsMeta: AnonymousConfigWithListId = AnonymousConfigWithListId(),
    val anonVariablesMeta: AnonymousConfigWithListId = AnonymousConfigWithListId(),
    val anonFunctionsMeta: AnonymousConfigWithListId = AnonymousConfigWithListId(),
    val anonClassesMeta: AnonymousConfigWithListId = AnonymousConfigWithListId()
) {

    fun resetIds() {
        anonVariablesMeta.resetIds()
        anonArgumentsMeta.resetIds()
        anonFunctionsMeta.resetIds()
        anonClassesMeta.resetIds()
    }

    fun findLabel(label: String, prefix: String): String? {
        // Try to find the current variable
        anonArgumentsMeta.getNewLabelByPrefix(label, prefix)?.let { return it }
        anonVariablesMeta.getNewLabelByPrefix(label, prefix)?.let { return it }
        anonFunctionsMeta.getNewLabelByPrefix(label, prefix)?.let { return it }

        /*
        * Try to find the current variable into names. For example if we have the function:
        *
        * def foo():
        *    print(foo)
        *    print(foo())
        *
        * We have to find foo in the names of anonFunctionsMeta (or in the anonClassesMeta if it id a class)
        */
        anonFunctionsMeta.getNewLabelByLabel(label, prefix)?.let { return it }
        anonClassesMeta.getNewLabelByLabel(label, prefix)?.let { return it }
        return null
    }

    // Todo: thinking about names
    /*
    * See [NestedObjectType]
    * */
    @ExperimentalStdlibApi
    fun updateResetLastIdMapByNestedObjectType(node: ITree, nestedObjectType: NestedObjectType,
                                               resetLastIdMap: MutableMap<Int, List<KFunction<Unit>>>) {
        val levels = listOf(anonVariablesMeta, anonArgumentsMeta, anonFunctionsMeta, anonClassesMeta)
        val removeLastFunctions = mutableListOf<KFunction<Unit>>()
        levels.subList(0, nestedObjectType.nestedLevel).forEach{
            it.currentIdsList.add(0)
            removeLastFunctions.add(it::removeLast)
        }
        resetLastIdMap[node.children.last().id] = removeLastFunctions
    }
}

/*
* We have 4 levels to reset (or add) ids:
* - [1] variables
* - [2] arguments
* - [3] functions
* - [4] classes
* For each nested object type we should reset ifs on the different levels
* If a nested object is a function nested into another function, we should use only [1] and [2] levels.
* If a nested object is a function nested into a class, we should use [1], [2] and [3] levels.
* If a nested object is a class, we should use all levels.
* */
enum class NestedObjectType(val nestedLevel: Int) {
    CLASS(4),
    FUNCTION_INTO_CLASS(3),
    FUNCTION_INTO_FUNCTION(2)
}