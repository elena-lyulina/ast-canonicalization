package org.jetbrains.research.transformations.util


object XMLTransformer {

    fun makeNewXML(baseXML: String): String {
        return baseXML.replace("label","value").replace("<root>", "").replace("</root>", "")
    }

}