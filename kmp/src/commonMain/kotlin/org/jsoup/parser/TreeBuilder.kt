package org.jsoup.parser

import kotlinx.io.Reader
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

/**
 * @author Jonathan Hedley
 */
abstract class TreeBuilder {

    lateinit var parser: Parser
    var reader: CharacterReader? = null
    var tokeniser: Tokeniser? = null
    protected lateinit var doc: Document // current doc we are building into
    var stack: ArrayList<Element?>? = null // the stack of open elements
    lateinit var baseUri: String // current base uri, for creating new elements

    protected var currentToken // currentToken is used only for error tracking.
            : Token? = null
    var settings: ParseSettings? = null
    protected var seenTags // tags we've used in this parse; saves tag GC for custom tags.
            : MutableMap<String, Tag?>? = null
    private val start: Token.StartTag = Token.StartTag() // start tag to process
    private val end: Token.EndTag = Token.EndTag()

    abstract fun defaultSettings(): ParseSettings

    protected open fun initialiseParse(input: Reader, baseUri: String, parser: Parser) {
        doc = Document(baseUri)
        doc.parser(parser)
        this.parser = parser
        settings = parser.settings()
        reader = CharacterReader(input)
        reader!!.trackNewlines(parser.isTrackErrors()) // when tracking errors, enable newline tracking for better error reports
        currentToken = null
        tokeniser = Tokeniser(reader!!, parser.getErrors())
        stack = ArrayList<Element?>(32)
        seenTags = HashMap<String, Tag?>()
        this.baseUri = baseUri
    }

    fun parse(input: Reader, baseUri: String, parser: Parser): Document {
        initialiseParse(input, baseUri, parser)
        runParser()

        // tidy up - as the Parser and Treebuilder are retained in document for settings / fragments
        reader!!.close()
        reader = null
        tokeniser = null
        stack = null
        seenTags = null
        return doc
    }

    /**
     * Create a new copy of this TreeBuilder
     * @return copy, ready for a new parse
     */
    abstract fun newInstance(): TreeBuilder

    abstract fun parseFragment(
        inputFragment: String,
        context: Element?,
        baseUri: String,
        parser: Parser
    ): List<Node>

    protected fun runParser() {
        while (true) {
            val token = tokeniser!!.read()
            process(token)
            token.reset()
            if (token.type === Token.TokenType.EOF) break
        }
    }

    abstract fun process(token: Token): Boolean

    fun processStartTag(name: String): Boolean {
        return if (currentToken === start) {
            // don't recycle an in-use token
            process(Token.StartTag().name(name))
        } else {
            process(start.reset().name(name))
        }
    }

    fun processStartTag(name: String, attrs: Attributes?): Boolean {
        if (currentToken === start) { // don't recycle an in-use token
            return process(Token.StartTag().nameAttr(name, attrs))
        }
        start.reset()
        start.nameAttr(name, attrs)
        return process(start)
    }

    fun processEndTag(name: String): Boolean {
        return if (currentToken === end) { // don't recycle an in-use token
            process(Token.EndTag().name(name))
        } else process(end.reset().name(name))
    }

    /**
     * Get the current element (last on the stack). If all items have been removed, returns the document instead
     * (which might not actually be on the stack; use stack.size() == 0 to test if required.
     * @return the last element on the stack, if any; or the root document
     */
    fun currentElement(): Element {
        val size = stack!!.size
        return if (size > 0) stack!![size - 1]!! else doc
    }

    /**
     * Checks if the Current Element's normal name equals the supplied name.
     * @param normalName name to check
     * @return true if there is a current element on the stack, and its name equals the supplied
     */
    fun currentElementIs(normalName: String): Boolean {
        if (stack!!.size == 0) return false
        val current = currentElement()
        return current.normalName().equals(normalName)
    }

    /**
     * If the parser is tracking errors, add an error at the current position.
     * @param msg error message
     */
    protected fun error(msg: String?) {
        error(msg, *(null as Array<Any?>?)!!)
    }

    /**
     * If the parser is tracking errors, add an error at the current position.
     * @param msg error message template
     * @param args template arguments
     */
    protected fun error(msg: String?, vararg args: Any?) {
        val errors: ParseErrorList = parser.getErrors()
        if (errors.canAddError()) errors.add(ParseError(reader, msg))
    }

    /**
     * (An internal method, visible for Element. For HTML parse, signals that script and style text should be treated as
     * Data Nodes).
     */
    open fun isContentForTagData(normalName: String): Boolean {
        return false
    }

    fun tagFor(tagName: String, settings: ParseSettings): Tag {
        var tag: Tag? =
            seenTags!![tagName] // note that we don't normalize the cache key. But tag via valueOf may be normalized.
        if (tag == null) {
            tag = Tag.valueOf(tagName, settings)
            seenTags!![tagName] = tag
        }
        return tag
    }
}