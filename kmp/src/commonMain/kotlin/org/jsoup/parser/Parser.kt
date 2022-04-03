package org.jsoup.parser

import kotlinx.io.Reader
import kotlinx.io.StringReader
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

/**
 * Parses HTML into a [org.jsoup.nodes.Document]. Generally best to use one of the  more convenient parse methods
 * in [org.jsoup.Jsoup].
 */
class Parser {

    private var treeBuilder: TreeBuilder
    private var errors: ParseErrorList
    private var settings: ParseSettings

    /**
     * Create a new Parser, using the specified TreeBuilder
     * @param treeBuilder TreeBuilder to use to parse input into Documents.
     */
    constructor(treeBuilder: TreeBuilder) {
        this.treeBuilder = treeBuilder
        settings = treeBuilder.defaultSettings()
        errors = ParseErrorList.noTracking()
    }

    /**
     * Creates a new Parser as a deep copy of this; including initializing a new TreeBuilder. Allows independent (multi-threaded) use.
     * @return a copied parser
     */
    fun newInstance(): Parser {
        return Parser(this)
    }

    private constructor(copy: Parser) {
        treeBuilder = copy.treeBuilder.newInstance() // because extended
        errors = ParseErrorList(copy.errors) // only copies size, not contents
        settings = ParseSettings(copy.settings)
    }

    fun parseInput(html: String, baseUri: String): Document {
        return treeBuilder.parse(StringReader(html), baseUri, this)
    }

    fun parseInput(inputHtml: Reader, baseUri: String): Document {
        return treeBuilder.parse(inputHtml, baseUri, this)
    }

    fun parseFragmentInput(fragment: String, context: Element?, baseUri: String): List<Node> {
        return treeBuilder.parseFragment(fragment, context, baseUri, this)
    }
    // gets & sets
    /**
     * Get the TreeBuilder currently in use.
     * @return current TreeBuilder.
     */
    fun getTreeBuilder(): TreeBuilder {
        return treeBuilder
    }

    /**
     * Update the TreeBuilder used when parsing content.
     * @param treeBuilder current TreeBuilder
     * @return this, for chaining
     */
    fun setTreeBuilder(treeBuilder: TreeBuilder): Parser {
        this.treeBuilder = treeBuilder
        treeBuilder.parser = this
        return this
    }

    /**
     * Check if parse error tracking is enabled.
     * @return current track error state.
     */
    fun isTrackErrors(): Boolean {
        return errors.getMaxSize() > 0
    }

    /**
     * Enable or disable parse error tracking for the next parse.
     * @param maxErrors the maximum number of errors to track. Set to 0 to disable.
     * @return this, for chaining
     */
    fun setTrackErrors(maxErrors: Int): Parser {
        errors = if (maxErrors > 0) ParseErrorList.tracking(maxErrors) else ParseErrorList.noTracking()
        return this
    }

    /**
     * Retrieve the parse errors, if any, from the last parse.
     * @return list of parse errors, up to the size of the maximum errors tracked.
     * @see .setTrackErrors
     */
    fun getErrors(): ParseErrorList {
        return errors
    }

    fun settings(settings: ParseSettings): Parser {
        this.settings = settings
        return this
    }

    fun settings(): ParseSettings {
        return settings
    }

    /**
     * (An internal method, visible for Element. For HTML parse, signals that script and style text should be treated as
     * Data Nodes).
     */
    fun isContentForTagData(normalName: String): Boolean {
        return getTreeBuilder().isContentForTagData(normalName)
    }

    companion object {
        // static parse functions below
        /**
         * Parse HTML into a Document.
         *
         * @param html HTML to parse
         * @param baseUri base URI of document (i.e. original fetch location), for resolving relative URLs.
         *
         * @return parsed Document
         */
        fun parse(html: String, baseUri: String): Document {
            val treeBuilder = HtmlTreeBuilder()
            return treeBuilder.parse(StringReader(html), baseUri, Parser(treeBuilder))
        }

        /**
         * Parse a fragment of HTML into a list of nodes. The context element, if supplied, supplies parsing context.
         *
         * @param fragmentHtml the fragment of HTML to parse
         * @param context (optional) the element that this HTML fragment is being parsed for (i.e. for inner HTML). This
         * provides stack context (for implicit element creation).
         * @param baseUri base URI of document (i.e. original fetch location), for resolving relative URLs.
         *
         * @return list of nodes parsed from the input HTML. Note that the context element, if supplied, is not modified.
         */
        fun parseFragment(fragmentHtml: String, context: Element?, baseUri: String): List<Node> {
            val treeBuilder = HtmlTreeBuilder()
            return treeBuilder.parseFragment(fragmentHtml, context, baseUri, Parser(treeBuilder))
        }

        /**
         * Parse a fragment of HTML into a list of nodes. The context element, if supplied, supplies parsing context.
         *
         * @param fragmentHtml the fragment of HTML to parse
         * @param context (optional) the element that this HTML fragment is being parsed for (i.e. for inner HTML). This
         * provides stack context (for implicit element creation).
         * @param baseUri base URI of document (i.e. original fetch location), for resolving relative URLs.
         * @param errorList list to add errors to
         *
         * @return list of nodes parsed from the input HTML. Note that the context element, if supplied, is not modified.
         */
        fun parseFragment(
            fragmentHtml: String,
            context: Element?,
            baseUri: String,
            errorList: ParseErrorList
        ): List<Node> {
            val treeBuilder = HtmlTreeBuilder()
            val parser: Parser = Parser(treeBuilder)
            parser.errors = errorList
            return treeBuilder.parseFragment(fragmentHtml, context, baseUri, parser)
        }

        /**
         * Parse a fragment of XML into a list of nodes.
         *
         * @param fragmentXml the fragment of XML to parse
         * @param baseUri base URI of document (i.e. original fetch location), for resolving relative URLs.
         * @return list of nodes parsed from the input XML.
         */
        fun parseXmlFragment(fragmentXml: String, baseUri: String): List<Node> {
            val treeBuilder = XmlTreeBuilder()
            return treeBuilder.parseFragment(fragmentXml, baseUri, Parser(treeBuilder))
        }

        /**
         * Parse a fragment of HTML into the `body` of a Document.
         *
         * @param bodyHtml fragment of HTML
         * @param baseUri base URI of document (i.e. original fetch location), for resolving relative URLs.
         *
         * @return Document, with empty head, and HTML parsed into body
         */
        fun parseBodyFragment(bodyHtml: String, baseUri: String): Document {
            val doc: Document = Document.createShell(baseUri)
            val body: Element = doc.body()
            val nodeList: List<Node> = parseFragment(bodyHtml, body, baseUri)
            val nodes: Array<Node> = nodeList.toTypedArray() // the node list gets modified when re-parented
            for (i in nodes.size - 1 downTo 1) {
                nodes[i].remove()
            }
            for (node in nodes) {
                body.appendChild(node)
            }
            return doc
        }

        /**
         * Utility method to unescape HTML entities from a string
         * @param string HTML escaped string
         * @param inAttribute if the string is to be escaped in strict mode (as attributes are)
         * @return an unescaped string
         */
        fun unescapeEntities(string: String, inAttribute: Boolean): String {
            val tokeniser = Tokeniser(CharacterReader(string), ParseErrorList.noTracking())
            return tokeniser.unescapeEntities(inAttribute)
        }
        // builders
        /**
         * Create a new HTML parser. This parser treats input as HTML5, and enforces the creation of a normalised document,
         * based on a knowledge of the semantics of the incoming tags.
         * @return a new HTML parser.
         */
        fun htmlParser(): Parser {
            return Parser(HtmlTreeBuilder())
        }

        /**
         * Create a new XML parser. This parser assumes no knowledge of the incoming tags and does not treat it as HTML,
         * rather creates a simple tree directly from the input.
         * @return a new simple XML parser.
         */
        fun xmlParser(): Parser {
            return Parser(XmlTreeBuilder())
        }
    }
}