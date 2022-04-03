package org.jsoup.nodes

import kotlinx.io.IOException
import org.jsoup.SerializationException
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document.OutputSettings

/**
 * An XML Declaration.
 */
class XmlDeclaration(name: String, isProcessingInstruction: Boolean) : LeafNode() {

    // todo this impl isn't really right, the data shouldn't be attributes, just a run of text after the name
    private val isProcessingInstruction // <! if true, <? if false, declaration (and last data char should be ?)
            : Boolean

    override fun nodeName(): String {
        return "#declaration"
    }

    /**
     * Get the name of this declaration.
     * @return name of this declaration.
     */
    fun name(): String {
        return coreValue()!!
    }

    /**
     * Get the unencoded XML declaration.
     * @return XML declaration
     */
    fun getWholeDeclaration(): String {
        val sb: StringBuilder = StringUtil.borrowBuilder()
        try {
            getWholeDeclaration(sb, OutputSettings())
        } catch (e: IOException) {
            throw SerializationException(e)
        }
        return StringUtil.releaseBuilder(sb).trim()
    }

    @Throws(IOException::class)
    private fun getWholeDeclaration(accum: Appendable, out: OutputSettings) {
        for (attribute in attributes()) {
            val key: String = attribute.key
            val `val`: String = attribute.value
            if (key != nodeName()) { // skips coreValue (name)
                accum.append(' ')
                // basically like Attribute, but skip empty vals in XML
                accum.append(key)
                if (!`val`.isEmpty()) {
                    accum.append("=\"")
                    Entities.escape(accum, `val`, out, true, false, false)
                    accum.append('"')
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun outerHtmlHead(accum: Appendable, depth: Int, out: OutputSettings) {
        accum
            .append("<")
            .append(if (isProcessingInstruction) "!" else "?")
            .append(coreValue())
        getWholeDeclaration(accum, out)
        accum
            .append(if (isProcessingInstruction) "!" else "?")
            .append(">")
    }

    override fun outerHtmlTail(accum: Appendable, depth: Int, out: OutputSettings) {}
    override fun toString(): String {
        return outerHtml()
    }

    /**
     * Create a new XML declaration
     * @param name of declaration
     * @param isProcessingInstruction is processing instruction
     */
    init {
        value = name
        this.isProcessingInstruction = isProcessingInstruction
    }
}