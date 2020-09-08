package org.jetbrains.research.transformations.anonimization

import com.github.gumtreediff.tree.ITree

interface IdCounter {
    val currentId: Int
}

data class AnonymousConfig(
        override var currentId: Int = 0,
        // old_label -> list[(new_label, parent_prefix)]
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
        // old_label -> new_label
        val namesMap: MutableMap<String, String> = mutableMapOf()
) : IdCounter

data class AnonymizationMetaInformation(
        val anonVariablesMeta: AnonymousConfig = AnonymousConfig(),
        val anonArgumentsMeta: AnonymousConfig = AnonymousConfig(),
        val anonFunctionsMeta: AnonymousFunctionsConfig = AnonymousFunctionsConfig()
)