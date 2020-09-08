package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext

val TreeContext.nodes: List<ITree>
    get() = run {
        val treeIterator = this.root.trees.iterator()
        val nodes = mutableListOf<ITree>()

        while (treeIterator.hasNext()) {
            val node = treeIterator.next()
            nodes.add(node)
        }
        nodes
    }

