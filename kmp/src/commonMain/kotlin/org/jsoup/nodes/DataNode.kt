package org.jsoup.nodes

import kotlin.Throws
import org.jsoup.nodes.Document.OutputSettings
import kotlinx.io.IOException

/**
 * A data node, for contents of style, script tags etc, where contents should not show in text().
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
class DataNode(data: String) : LeafNode() {

    override fun nodeName(): String {
        return "#data"
    }

    /**
     * Get the data contents of this node. Will be unescaped and with original new lines, space etc.
     * @return data
     */
    val wholeData: String
        get() = coreValue()!!

    /**
     * Set the data contents of this node.
     * @param data unencoded data
     * @return this node, for chaining
     */
    fun setWholeData(data: String?): DataNode {
        coreValue(data)
        return this
    }

    @Throws(IOException::class)
    override fun outerHtmlHead(accum: Appendable, depth: Int, out: OutputSettings) {
        accum.append(wholeData) // data is not escaped in return from data nodes, so " in script, style is plain
    }

    override fun outerHtmlTail(accum: Appendable, depth: Int, out: OutputSettings) {}
    override fun toString(): String {
        return outerHtml()
    }

    /**
     * Create a new DataNode.
     * @param data data contents
     */
    init {
        value = data
    }
}