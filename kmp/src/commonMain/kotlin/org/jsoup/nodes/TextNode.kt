package org.jsoup.nodes

import kotlinx.io.IOException
import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document.OutputSettings

/**
 * A text node.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
open class TextNode(text: String) : LeafNode() {

    /**
     * Create a new TextNode representing the supplied (unencoded) text).
     *
     * @param text raw text
     * @see .createFromEncoded
     */
    init {
        value = text
    }

    override fun nodeName(): String {
        return "#text"
    }

    /**
     * Get the text content of this text node.
     * @return Unencoded, normalised text.
     * @see TextNode.getWholeText
     */
    open fun text(): String {
        return StringUtil.normaliseWhitespace(wholeText)
    }

    /**
     * Set the text content of this text node.
     * @param text unencoded text
     * @return this, for chaining
     */
    fun text(text: String?): TextNode {
        coreValue(text)
        return this
    }

    val wholeText: String
        get() = coreValue()!!

    /**
     * Test if this text node is blank -- that is, empty or only whitespace (including newlines).
     * @return true if this document is empty or only whitespace, false if it contains any text content.
     */
    fun isBlank(): Boolean {
        return StringUtil.isBlank(coreValue())
    }

    /**
     * Split this text node into two nodes at the specified string offset. After splitting, this node will contain the
     * original text up to the offset, and will have a new text node sibling containing the text after the offset.
     * @param offset string offset point to split node at.
     * @return the newly created text node containing the text after the offset.
     */
    fun splitText(offset: Int): TextNode {
        val text: String = coreValue()!!
        Validate.isTrue(offset >= 0, "Split offset must be not be negative")
        Validate.isTrue(offset < text.length, "Split offset must not be greater than current text length")
        val head = text.substring(0, offset)
        val tail = text.substring(offset)
        text(head)
        val tailNode = TextNode(tail)
        if (parentNode != null) {
            parentNode!!.addChildren(siblingIndex + 1, tailNode)
        }
        return tailNode
    }

    @Throws(IOException::class)
    override fun outerHtmlHead(accum: Appendable, depth: Int, out: OutputSettings) {
        val prettyPrint: Boolean = out.prettyPrint()
        val parent: Element? = if (parentNode is Element) parentNode as Element? else null
        val parentIndent = parent != null && parent.shouldIndent(out)
        val blank = isBlank()
        if (parentIndent && StringUtil.startsWithNewline(coreValue()) && blank) // we are skippable whitespace
            return
        if (prettyPrint && (siblingIndex == 0 && parent != null && parent.tag()
                .formatAsBlock() && !blank || out.outline() && siblingNodes().size > 0 && !blank)
        ) indent(accum, depth, out)
        val normaliseWhite = prettyPrint && !Element.preserveWhitespace(parentNode)
        val stripWhite = prettyPrint && parentNode is Document
        Entities.escape(accum, coreValue()!!, out, false, normaliseWhite, stripWhite)
    }

    override fun outerHtmlTail(accum: Appendable, depth: Int, out: OutputSettings) {}
    override fun toString(): String {
        return outerHtml()
    }

    companion object {

        /**
         * Create a new TextNode from HTML encoded (aka escaped) data.
         * @param encodedText Text containing encoded HTML (e.g. &amp;lt;)
         * @return TextNode containing unencoded data (e.g. &lt;)
         */
        fun createFromEncoded(encodedText: String): TextNode {
            val text: String = Entities.unescape(encodedText)
            return TextNode(text)
        }

        fun normaliseWhitespace(text: String): String {
            var text = text
            text = StringUtil.normaliseWhitespace(text)
            return text
        }

        fun stripLeadingWhitespace(text: String): String {
            return text.replaceFirst("^\\s+".toRegex(), "")
        }

        fun lastCharIsWhitespace(sb: StringBuilder): Boolean {
            return sb.length != 0 && sb[sb.length - 1] == ' '
        }
    }
}