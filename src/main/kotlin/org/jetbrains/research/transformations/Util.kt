package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

fun TreeContext.transformAndGetNodes(transformationFunction: (ITree) -> Unit = {}): List<ITree> {
    val treeIterator = this.root.trees.iterator()
    val nodes = mutableListOf<ITree>()

    while (treeIterator.hasNext()) {
        val node = treeIterator.next()
        transformationFunction(node)
        nodes.add(node)
    }
    return nodes
}

