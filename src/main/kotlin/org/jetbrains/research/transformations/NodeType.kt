package org.jetbrains.research.transformations

enum class NodeType(val type: String) {
    // Variable definition
    NAME_STORE("Name_Store"),
    // An existing variable/function and etc
    NAME_LOAD("Name_Load"),
    // Argument of function
    ARG("arg"),
    // Function definition
    FUNC_DEF("FunctionDef")
}