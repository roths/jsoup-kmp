package org.jsoup.nodes

import kotlinx.io.IOException
import org.jsoup.UncheckedIOException
import org.jsoup.nodes.Document.OutputSettings

/**
 * A Character Data node, to support CDATA sections.
 */
class CDataNode(text: String) : TextNode(text) {

    override fun nodeName(): String {
        return "#cdata"
    }

    /**
     * Get the unencoded, **non-normalized** text content of this CDataNode.
     * @return unencoded, non-normalized text
     */
    override fun text(): String {
        return wholeText
    }

    @Throws(IOException::class)
    override fun outerHtmlHead(accum: Appendable, depth: Int, out: OutputSettings) {
        accum
            .append("<![CDATA[")
            .append(wholeText)
    }

    override fun outerHtmlTail(accum: Appendable, depth: Int, out: OutputSettings) {
        try {
            accum.append("]]>")
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}