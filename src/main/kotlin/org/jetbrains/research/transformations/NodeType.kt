package org.jetbrains.research.transformations

enum class NodeType(val type: String) {
    NAME_STORE("Name_Store"),
    NAME_LOAD("Name_Load"),
    ARG("arg"),
    FUNC_DEF("FunctionDef")
}