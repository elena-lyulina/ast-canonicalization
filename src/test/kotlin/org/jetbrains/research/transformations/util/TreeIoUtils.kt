package org.jetbrains.research.transformations.util

import com.github.gumtreediff.io.IndentingXMLStreamWriter
import com.github.gumtreediff.io.TreeIoUtils.*
import com.github.gumtreediff.tree.ITree
import com.github.gumtreediff.tree.TreeContext
import com.github.gumtreediff.tree.TreeContext.MetadataSerializers
import java.io.Writer
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

open class XMLFormatter(private val context: TreeContext, w: Writer) : TreeFormatter {
    private var writer: XMLStreamWriter? = null

    init {
        val f = XMLOutputFactory.newInstance()
        writer = IndentingXMLStreamWriter(f.createXMLStreamWriter(w))
    }

    override fun startSerialization() {
        writer?.writeStartDocument()
    }

    override fun stopSerialization() {
        writer?.writeEndDocument()
    }

    override fun serializeAttribute(name: String?, value: String?) {
        writer?.writeAttribute(name, value)
    }

    override fun startTree(tree: ITree?) {
        tree?.let {
            if (tree.children?.size == 0)
                writer?.writeEmptyElement(context.getTypeLabel(tree.type))
            else writer?.writeStartElement(tree.type.let { context.getTypeLabel(it) })
            if (tree.hasLabel()) writer?.writeAttribute("value", tree.label)
        }
    }

    override fun endTree(tree: ITree?) {
        tree?.let {
            if (tree.children.size > 0)
                writer?.writeEndElement()
        }
    }

    override fun close() {
        writer?.close()
    }

    override fun endProlog() {
    }

    override fun endTreeProlog(tree: ITree?) {
    }

}

object TreeIoUtils {
    fun toXMLWithoutRoot(ctx: TreeContext) : TreeSerializer {
        return object : TreeSerializer(ctx) {
            @Throws(Exception::class)
            override fun newFormatter(
                ctx: TreeContext,
                serializers: MetadataSerializers?,
                writer: Writer
            ): TreeFormatter? {
                return XMLFormatter(ctx, writer)
            }
        }
    }
}