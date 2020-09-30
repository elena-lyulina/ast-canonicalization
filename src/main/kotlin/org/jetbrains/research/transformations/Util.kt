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
    return object : Iterable<ITree> {
        override fun iterator(): Iterator<ITree> {
            return object : Iterator<ITree> {
                val currentNodes: Stack<ITree> = Stack()

                init {
                    currentNodes.addAll(this@dfs.children.reversed())
                }

                override fun hasNext(): Boolean {
                    return currentNodes.size != 0
                }

                override fun next(): ITree {
                    val c = currentNodes.pop()
                    currentNodes.addAll(c.children.reversed())
                    return c
                }

            }
        }
    }
}