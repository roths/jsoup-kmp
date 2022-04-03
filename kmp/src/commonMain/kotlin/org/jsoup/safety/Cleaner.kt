package org.jsoup.safety

import org.jsoup.helper.Validate
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.ParseErrorList
import org.jsoup.parser.Parser
import org.jsoup.parser.Tag
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor

/**
 * The safelist based HTML cleaner. Use to ensure that end-user provided HTML contains only the elements and attributes
 * that you are expecting; no junk, and no cross-site scripting attacks!
 *
 *
 * The HTML cleaner parses the input as HTML and then runs it through a safe-list, so the output HTML can only contain
 * HTML that is allowed by the safelist.
 *
 *
 *
 * It is assumed that the input HTML is a body fragment; the clean methods only pull from the source's body, and the
 * canned safe-lists only allow body contained tags.
 *
 *
 *
 * Rather than interacting directly with a Cleaner object, generally see the `clean` methods in [org.jsoup.Jsoup].
 *
 */
class Cleaner(safelist: Safelist) {
    /**
     * Create a new cleaner, that sanitizes documents using the supplied safelist.
     * @param safelist safe-list to clean with
     */
    private val safelist: Safelist = safelist

    /**
     * Creates a new, clean document, from the original dirty document, containing only elements allowed by the safelist.
     * The original document is not modified. Only elements from the dirty document's `body` are used. The
     * OutputSettings of the original document are cloned into the clean document.
     * @param dirtyDocument Untrusted base document to clean.
     * @return cleaned document.
     */
    fun clean(dirtyDocument: Document): Document {
        Validate.notNull(dirtyDocument)
        val clean: Document = Document.createShell(dirtyDocument.baseUri())
        copySafeNodes(dirtyDocument.body(), clean.body())
        clean.outputSettings(dirtyDocument.outputSettings().clone())
        return clean
    }

    /**
     * Determines if the input document **body**is valid, against the safelist. It is considered valid if all the tags and attributes
     * in the input HTML are allowed by the safelist, and that there is no content in the `head`.
     *
     *
     * This method can be used as a validator for user input. An invalid document will still be cleaned successfully
     * using the [.clean] document. If using as a validator, it is recommended to still clean the document
     * to ensure enforced attributes are set correctly, and that the output is tidied.
     *
     * @param dirtyDocument document to test
     * @return true if no tags or attributes need to be removed; false if they do
     */
    fun isValid(dirtyDocument: Document): Boolean {
        Validate.notNull(dirtyDocument)
        val clean: Document = Document.createShell(dirtyDocument.baseUri())
        val numDiscarded = copySafeNodes(dirtyDocument.body(), clean.body())
        return (numDiscarded == 0
                && dirtyDocument.head().childNodes()
            .isEmpty() // because we only look at the body, but we start from a shell, make sure there's nothing in the head
                )
    }

    fun isValidBodyHtml(bodyHtml: String): Boolean {
        val clean: Document = Document.createShell("")
        val dirty: Document = Document.createShell("")
        val errorList = ParseErrorList.tracking(1)
        val nodes: List<Node> = Parser.parseFragment(bodyHtml, dirty.body(), "", errorList)
        dirty.body().insertChildren(0, nodes)
        val numDiscarded = copySafeNodes(dirty.body(), clean.body())
        return numDiscarded == 0 && errorList.isEmpty()
    }

    /**
     * Iterates the input and copies trusted nodes (tags, attributes, text) into the destination.
     */
    private inner class CleaningVisitor(root: Element, destination: Element) : NodeVisitor {

        var numDiscarded = 0
        private val root: Element
        private var destination // current element to append nodes to
                : Element

        override fun head(source: Node, depth: Int) {
            if (source is Element) {
                val sourceEl: Element = source as Element
                if (safelist.isSafeTag(sourceEl.normalName())) { // safe, clone and copy safe attrs
                    val meta = createSafeElement(sourceEl)
                    val destChild: Element = meta.el
                    destination.appendChild(destChild)
                    numDiscarded += meta.numAttribsDiscarded
                    destination = destChild
                } else if (source !== root) { // not a safe tag, so don't add. don't count root against discarded.
                    numDiscarded++
                }
            } else if (source is TextNode) {
                val sourceText: TextNode = source as TextNode
                val destText = TextNode(sourceText.wholeText)
                destination.appendChild(destText)
            } else if (source is DataNode && safelist.isSafeTag(source.parent()!!.nodeName())) {
                val sourceData: DataNode = source
                val destData = DataNode(sourceData.wholeData)
                destination.appendChild(destData)
            } else { // else, we don't care about comments, xml proc instructions, etc
                numDiscarded++
            }
        }

        override fun tail(source: Node, depth: Int) {
            if (source is Element && safelist.isSafeTag(source.nodeName())) {
                destination = destination.parent()!! // would have descended, so pop destination stack
            }
        }

        init {
            this.root = root
            this.destination = destination
        }
    }

    private fun copySafeNodes(source: Element, dest: Element): Int {
        val cleaningVisitor = CleaningVisitor(source, dest)
        NodeTraversor.traverse(cleaningVisitor, source)
        return cleaningVisitor.numDiscarded
    }

    private fun createSafeElement(sourceEl: Element): ElementMeta {
        val sourceTag: String = sourceEl.tagName()
        val destAttrs = Attributes()
        val dest = Element(Tag.valueOf(sourceTag), sourceEl.baseUri(), destAttrs)
        var numDiscarded = 0
        val sourceAttrs: Attributes = sourceEl.attributes()
        for (sourceAttr in sourceAttrs) {
            if (safelist.isSafeAttribute(
                    sourceTag,
                    sourceEl,
                    sourceAttr
                )
            ) destAttrs.put(sourceAttr) else numDiscarded++
        }
        val enforcedAttrs: Attributes = safelist.getEnforcedAttributes(sourceTag)
        destAttrs.addAll(enforcedAttrs)
        return ElementMeta(dest, numDiscarded)
    }

    private class ElementMeta internal constructor(el: Element, numAttribsDiscarded: Int) {

        var el: Element
        var numAttribsDiscarded: Int

        init {
            this.el = el
            this.numAttribsDiscarded = numAttribsDiscarded
        }
    }
}