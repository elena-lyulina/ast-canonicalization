package org.jetbrains.research.transformations

import com.github.gumtreediff.tree.ITree

public class Transformations() {
    public val var_anon : MutableMap<String, String> = mutableMapOf()
    private var num_var_anon = 0

    // this method should anonymize all variables in code
    fun anonymize(tree :ITree, toStoreMetadata :Boolean) {
        if (!tree.isLeaf) {
            for (node in tree.children) {

                // if type is "Store" (80218305)
                if (node.type == 80218305){
                    if (node.label !in var_anon.keys) {
                        var_anon[node.label] = "v" + num_var_anon
                        node.setMetadata(var_anon[node.label], node.label)
                        node.label = var_anon[node.label]
                        num_var_anon += 1
                    }
                }

                // if type is "Load" (2373894)
                if (node.type == 2373894) {
                    if (node.label in var_anon.keys) {
                        node.label = var_anon[node.label]
                    }
                }

                anonymize(node, true)
            }
        }
        return
    }
}