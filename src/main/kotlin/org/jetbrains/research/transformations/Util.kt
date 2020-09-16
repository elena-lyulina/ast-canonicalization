package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext
import java.util.*
import kotlin.reflect.KFunction1

fun TreeContext.transformAndGetNodes(
    transformationFunction: (ITree) -> Unit = {},
    traversal: KFunction1<ITree, Iterable<ITree>> = ITree::dfs
): List<ITree> {
    val treeIterator = traversal(this.root).iterator()
    val nodes = mutableListOf<ITree>()

    while (treeIterator.hasNext()) {
        val node = treeIterator.next()
        transformationFunction(node)
        nodes.add(node)
    }
    return nodes
}

fun ITree.dfs(): Iterable<ITree> {
    val traversal: MutableList<ITree> = ArrayList()
    val currentNodes: Stack<ITree> = Stack()
    this.children.reversed().forEach { currentNodes.push(it) }
    while (!currentNodes.empty()) {
        val c = currentNodes.pop()
        traversal.add(c)
        c.children.reversed().forEach { currentNodes.push(it) }
    }
    return traversal
}


