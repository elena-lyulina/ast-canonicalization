package org.jetbrains.research.transformations

import kotlin.reflect.KClass

/*
* [NodeType] stores all possible nodes' types in Python AST that are connected to transformations. This is usually the
* root of some subtree - declaring or using a variable, declaring a class or function, and so on. The [typeLabel] stores
* label from the XML tree from pythonparser.
* */
enum class NodeType(val typeLabel: String) {
    // Variable definition
    NAME_STORE("Name_Store"),
    // An existing variable/function and etc
    NAME_LOAD("Name_Load"),
    // An existing list
    LIST_LOAD("List_Load"),
    // Class definition
    CLASS_DEF("ClassDef"),
    // Function definition
    FUNC_DEF("FunctionDef"),
    // Function body
    FUNC_BODY("body"),
    // All argument of function
    ARGUMENTS("arguments"),
    // Argument of function
    ARG("arg"),
    RETURN("Return")

}

enum class OperatorKey(val key: String) {
    /*
    * To understand what means EXPR and CALL see the following XML for code `print(5)`:
     <Module lineno="1" col="0" end_line_no="1" end_col="8">
        <Expr lineno="1" col="0" end_line_no="1" end_col="8">
            <Call lineno="1" col="0" end_line_no="1" end_col="8">
                <Name_Load value="print" lineno="1" col="0" end_line_no="1" end_col="5">
                </Name_Load>
                <Constant-int value="5" lineno="1" col="6" end_line_no="1" end_col="7">
                </Constant-int>
            </Call>
        </Expr>
    </Module>
    */
    EXPR("Expr"),
    CALL("Call"),

    UNARY_OP("UnaryOp"),
    BIN_OP("BinOp"),
    BOOL_OP("BoolOp"),
    COMPARE("Compare")
}

enum class OperatorName(val key: String) {
    // Same as a + b
    ADDITION("Add"),
    // Same as a - b
    SUBTRACTION("Sub"),
    // Same as a * b
    MULTIPLICATION("Mult"),
    // Same as a @ b, see matmul operator
    MATRIX_MULTIPLICATION("MatMult"),
    // Same as a / b
    DIVISION("Div"),
    // Same as a // b
    FLOOR_DIVISION("FloorDiv"),
    // Same as a % b
    MOD("Mod"),
    // Same as a % b
    POW("Pow"),
    // Same as a << b
    LEFT_SHIFT("LShift"),
    // Same as a >> b
    RIGHT_SHIFT("RShift"),
    // Same as a | b
    OR("BitOr"),
    // Same as a ^ b
    XOR("BitXor"),
    // Same as a & b
    AND("BitAnd"),
    // Same as a == b
    EQUAL("Eq"),
    // Same as a > b
    GT("Gt"),
    // Same as a >= b
    GT_EQUAL("GtE"),
    // Same as a < b
    LT("Lt"),
    // Same as a <= b
    LT_EQUAL("LtE")

}

// Todo: add descriptions or examples
enum class LiteralType(val key: String) {
    // Const value
    CONST("Constant"),
    NUM("Num"),
    STR("Str"),
    BYTES("Bytes"),
    LIST("List"),
    TUPLE("Tuple"),
    SET("Set"),
    DICT("Dict"),
    NAME_CONST("NameConstant"),
}

enum class Type(val pythonKey: String, kotlinType: KClass<*>) {
    INT("int", Int::class),
    FLOAT("float", Float::class),
    BOOL("bool", Boolean::class)
}