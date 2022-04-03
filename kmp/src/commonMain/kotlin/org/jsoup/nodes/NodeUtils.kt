package org.jsoup.nodes

import org.jsoup.nodes.Document.OutputSettings
import org.jsoup.parser.HtmlTreeBuilder
import org.jsoup.parser.Parser

/**
 * Internal helpers for Nodes, to keep the actual node APIs relatively clean. A jsoup internal class, so don't use it as
 * there is no contract API).
 */
internal object NodeUtils {

    /**
     * Get the output setting for this node,  or if this node has no document (or parent), retrieve the default output
     * settings
     */
    fun outputSettings(node: Node): OutputSettings {
        val owner = node.ownerDocument()
        return if (owner != null) owner.outputSettings() else Document("").outputSettings()
    }

    /**
     * Get the parser that was used to make this node, or the default HTML parser if it has no parent.
     */
    fun parser(node: Node): Parser {
        val doc = node.ownerDocument()
        return if (doc != null && doc.parser() != null) doc.parser() else Parser(HtmlTreeBuilder())
    }

}