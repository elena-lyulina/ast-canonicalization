package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.TreeContext

/**
 * The basic interface for AST transformations
 */
interface Transformation {

    val metadataKey: String

    /**
     * Applying forward transformation.
     * @param [treeCtx] - Abstract syntax tree, built on a source code.
     * @param [toStoreMetadata] - flag that indicates necessity
     * of metadata storing.
     */
    fun apply(treeCtx: TreeContext, toStoreMetadata: Boolean)

    /**
     * Applying reverse transformation.
     * @param [treeCtx] - Abstract syntax tree, built on source code.
     */
    fun inverseApply(treeCtx: TreeContext)

}