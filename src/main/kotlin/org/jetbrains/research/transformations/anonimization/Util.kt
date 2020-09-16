package org.jetbrains.research.transformations.anonimization

import com.github.gumtreediff.tree.ITree

interface Identifiable {
    fun getLastId(): Int

    fun resetId()
}

interface IntIdCounter : Identifiable {
    var currentId: Int

    override fun getLastId(): Int {
        return currentId
    }

    override fun resetId() {
        currentId = 0
    }
}

interface ListIdCounter : Identifiable {
    var currentIdsList: MutableList<Int>

    override fun getLastId(): Int {
        return currentIdsList.last()
    }

    override fun resetId() {
        currentIdsList = mutableListOf(currentIdsList.first())
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

data class AnonymousConfigWithIntId(
    override var currentId: Int = 0,
    override val namesMap: MutableMap<String, List<Pair<String, String>>> = mutableMapOf()
) : IntIdCounter, AnonymousNamesMap(namesMap) {

    override fun initLabel(oldLabel: String, newLabel: String, parentPrefix: String) {
        namesMap[oldLabel] = listOf(Pair(newLabel, parentPrefix))
        currentId += 1
    }
}


data class AnonymousConfigWithListId(
    override var currentIdsList: MutableList<Int> = mutableListOf(0),
    override val namesMap: MutableMap<String, List<Pair<String, String>>> = mutableMapOf()
) : ListIdCounter, AnonymousNamesMap(namesMap) {

    override fun initLabel(oldLabel: String, newLabel: String, parentPrefix: String) {
        namesMap[oldLabel] = listOf(Pair(newLabel, parentPrefix))
        currentIdsList[currentIdsList.size - 1]++
    }
}

data class AnonymizationMetaInformation(
    val anonVariablesMeta: AnonymousConfigWithIntId = AnonymousConfigWithIntId(),
    val anonArgumentsMeta: AnonymousConfigWithIntId = AnonymousConfigWithIntId(),
    val anonFunctionsMeta: AnonymousConfigWithListId = AnonymousConfigWithListId()
) {

    fun resetIds() {
        anonVariablesMeta.resetId()
        anonArgumentsMeta.resetId()
        anonFunctionsMeta.resetId()
    }

    fun findLabel(label: String, prefix: String): String? {
        // Try to find the current variable
        anonArgumentsMeta.getNewLabelByPrefix(label, prefix)?.let { return it }
        anonVariablesMeta.getNewLabelByPrefix(label, prefix)?.let { return it }
        anonFunctionsMeta.getNewLabelByLabel(label, prefix)?.let { return it }
        return null
    }
}