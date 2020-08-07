package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.TreeContext

/**
 * The basic interface for AST transformations
 */
interface Transformation {

    abstract val metadataKey: String

    /**
     * Applying forward transformation.
     * @param [tree] - Abstract syntax tree, built on a source code.
     * @param [toStoreMetadata] - flag that indicates necessity
     * of metadata storing.
     */
    fun apply(tree: TreeContext, toStoreMetadata: Boolean)

    /**
     * Applying reverse transformation.
     * @param [tree] - Abstract syntax tree, built on source code.
     */
    fun reverseApply(tree: TreeContext)

}