package org.jetbrains.research.transformations.anonimization

import com.github.gumtreediff.tree.ITree

interface IdCounter {
    val currentId: Int
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
data class AnonymousConfig(
        override var currentId: Int = 0,
        val namesMap: MutableMap<String, List<Pair<String, String>>> = mutableMapOf()
) : IdCounter {

    fun initLabel(oldLabel: String, newLabel: String, parentPrefix: String) {
        namesMap[oldLabel] = listOf(Pair(newLabel, parentPrefix))
        currentId += 1
    }

    fun getPrefixes(node: ITree): List<String> {
        return namesMap.getOrDefault(node.label, listOf()).map { (_, parent_prefix) -> parent_prefix }
    }
}


data class AnonymousFunctionsConfig(
        override var currentId: Int = 0,
        val namesMap: MutableMap<String, String> = mutableMapOf()
) : IdCounter

data class AnonymizationMetaInformation(
        val anonVariablesMeta: AnonymousConfig = AnonymousConfig(),
        val anonArgumentsMeta: AnonymousConfig = AnonymousConfig(),
        val anonFunctionsMeta: AnonymousFunctionsConfig = AnonymousFunctionsConfig()
)