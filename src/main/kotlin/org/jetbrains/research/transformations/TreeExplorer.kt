package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext
import java.lang.RuntimeException
import java.lang.reflect.Method
import java.util.*
import javax.xml.xpath.XPathEvaluationResult
import kotlin.jvm.internal.CallableReference
import kotlin.math.pow
import kotlin.reflect.KFunction1


class TreeExplorer(private val treeCtx: TreeContext) {

    private val constLikeTypes: List<String> = listOf("Constant", "Num",
        "Str", "NameConstant", "Bytes")

    private val strTypeReprToCaster = mapOf(
        "int" to Int
    )


    fun isBinOp(node: ITree): Boolean {
        return treeCtx.getTypeLabel(node).split("_")[0] == "BinOp"
    }

    fun isBoolOp(node: ITree): Boolean {
        return treeCtx.getTypeLabel(node).split("_")[0] == "BoolOp"
    }

    fun isUnaryOp(node: ITree): Boolean {
        return treeCtx.getTypeLabel(node).split("_")[0] == "UnaryOp"
    }

    fun isOperation(node: ITree): Boolean {
        return isBinOp(node) or isBoolOp(node) or isUnaryOp(node)
    }

    fun isConstLikeType(node: ITree): Boolean {
        return treeCtx.getTypeLabel(node).split("-")[0] in constLikeTypes
    }

    fun getStrValueRepr(node: ITree): String {
        return node.label
    }

    fun getOperationName(node: ITree): String {
        val nodeName = treeCtx.getTypeLabel(node)
        if (isOperation(node)) {
            return nodeName.split("_")[1]
        }
        else {
            throw IllegalArgumentException("$nodeName is not Operation node")
        }
    }

    fun getValueTypeName(node: ITree): String {
        val nodeName = treeCtx.getTypeLabel(node)

        if (nodeName == "Str") {
            return "str"
        }
        try {
            return nodeName.split("-")[1]
        }
        catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("No value type found in $nodeName node")
        }
    }

    fun getConstLikeTypeName(node: ITree): String {
        val nodeName = treeCtx.getTypeLabel(node)
        if (isConstLikeType(node)) {
            return nodeName.split("-")[0]
        }
        else {
            throw IllegalArgumentException("Expected const like type node, but $nodeName was given ")
        }

    }

    fun getBinOpResult(opName: String, left: ITree, right: ITree): OperationResult? {

        val leftOperandType = getValueTypeName(left)
        val rightOperandType = getValueTypeName(right)

        val strLeftOperand = getStrValueRepr(left)
        val strRightOperand = getStrValueRepr(right)

        val binOperationResult: Any?

        when (opName) {

            "Add" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "float") -> strLeftOperand.toInt() + strRightOperand.toFloat()
                    Pair("float", "int") -> strLeftOperand.toFloat() + strRightOperand.toInt()
                    Pair("int", "int") -> strLeftOperand.toInt() + strRightOperand.toInt()
                    Pair("float", "float") -> strLeftOperand.toFloat() + strRightOperand.toFloat()
                    Pair("str", "str") -> strLeftOperand + strRightOperand
                    else -> null
                }
            }

            "Sub" -> { // just all "+" become "-"
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "float") -> strLeftOperand.toInt() - strRightOperand.toFloat()
                    Pair("float", "int") -> strLeftOperand.toFloat() - strRightOperand.toInt()
                    Pair("int", "int") -> strLeftOperand.toInt() - strRightOperand.toInt()
                    Pair("float", "float") -> strLeftOperand.toFloat() - strRightOperand.toFloat()
                    else -> null
                }
            }

            "Mult" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "float") -> strLeftOperand.toInt() * strRightOperand.toFloat()
                    Pair("float", "int") -> strLeftOperand.toFloat() * strRightOperand.toInt()
                    Pair("int", "int") -> strLeftOperand.toInt() * strRightOperand.toInt()
                    Pair("float", "float") -> strLeftOperand.toFloat() * strRightOperand.toFloat()
                    Pair("int", "str") -> strRightOperand.repeat(strLeftOperand.toInt())
                    Pair("str", "int") -> strLeftOperand.repeat(strRightOperand.toInt())
                    else -> null
                }
            }

            "Div" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "float") -> strLeftOperand.toInt().div(strRightOperand.toFloat())
                    Pair("float", "int") -> strLeftOperand.toFloat().div(strRightOperand.toInt())
                    Pair("int", "int") -> strLeftOperand.toInt().div(strRightOperand.toInt())
                    Pair("float", "float") -> strLeftOperand.toFloat().div(strRightOperand.toFloat())
                    else -> null
                }
            }

            "FloorDiv" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "int") -> Math.floorDiv(strLeftOperand.toInt(), strRightOperand.toInt())
                    else -> null
                }
            }

            "Mod" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "float") -> strLeftOperand.toInt().rem(strRightOperand.toFloat())
                    Pair("float", "int") -> strLeftOperand.toFloat().rem(strRightOperand.toInt())
                    Pair("int", "int") -> strLeftOperand.toInt().rem(strRightOperand.toInt())
                    Pair("float", "float") -> strLeftOperand.toFloat().rem(strRightOperand.toFloat())
                    else -> null
                }
            }

            "Pow" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "float") -> strLeftOperand.toFloat().pow(strRightOperand.toFloat())
                    Pair("float", "int") -> strLeftOperand.toFloat().pow(strRightOperand.toInt())
                    Pair("int", "int") -> strLeftOperand.toFloat().pow(strRightOperand.toInt()).toInt()
                    Pair("float", "float") -> strLeftOperand.toFloat().pow(strRightOperand.toFloat())
                    else -> null
                }

            }

            "LShift" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "int") -> strLeftOperand.toInt().shl(strRightOperand.toInt())
                    else -> null
                }
            }

            "RShift" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "int") -> strLeftOperand.toInt().shr(strRightOperand.toInt())
                    else -> null
                }
            }

            "BitOr" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "int") -> strLeftOperand.toInt().or(strRightOperand.toInt())
                    else -> null
                }
            }

            "BitXor" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "int") -> strLeftOperand.toInt().xor(strRightOperand.toInt())
                    else -> null
                }
            }

            "BitAnd" -> {
                binOperationResult = when (Pair(leftOperandType, rightOperandType)) {
                    Pair("int", "int") -> strLeftOperand.toInt().and(strRightOperand.toInt())
                    else -> null
                }
            }

            else -> {
                throw RuntimeException("Unknown BinOp: $opName")
            }
        }

        return OperationResult(binOperationResult, binOperationResult!!::class.simpleName)
    }
}

class OperationResult(var result: Any?, resultKotlinTypeName: String?) {
    var resultPythonTypeName: String? = when(resultKotlinTypeName) {
        "Int" -> "int"
        "Float" -> "float"
        "String" -> "str"
        else -> null
    }
}

