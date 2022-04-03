package org.jsoup.nodes

import kotlinx.io.IOException
import org.jsoup.SerializationException
import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document.OutputSettings
import org.jsoup.select.NodeFilter
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor

/**
 * The base, abstract Node model. Elements, Documents, Comments etc are all Node instances.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
abstract class Node protected constructor() {

    // Nodes don't always have parents
    var parentNode: Node? = null
        set(parentNode) {
            Validate.notNull(parentNode)
            if (this.parentNode != null) this.parentNode!!.removeChild(this)
            field = parentNode
        }

    var siblingIndex = 0

    /**
     * Get the node name of this node. Use for debugging purposes and not logic switching (for that, use instanceof).
     * @return node name
     */
    abstract fun nodeName(): String

    /**
     * Check if this Node has an actual Attributes object.
     */
    protected abstract fun hasAttributes(): Boolean

    /**
     * Checks if this node has a parent. Nodes won't have parents if (e.g.) they are newly created and not added as a child
     * to an existing node, or if they are a [.shallowClone]. In such cases, [.parent] will return `null`.
     * @return if this node has a parent.
     */
    fun hasParent(): Boolean {
        return parentNode != null
    }

    /**
     * Get an attribute's value by its key. **Case insensitive**
     *
     *
     * To get an absolute URL from an attribute that may be a relative URL, prefix the key with `**abs**`,
     * which is a shortcut to the [.absUrl] method.
     *
     * E.g.:
     * <blockquote>`String url = a.attr("abs:href");`</blockquote>
     *
     * @param attributeKey The attribute key.
     * @return The attribute, or empty string if not present (to avoid nulls).
     * @see .attributes
     * @see .hasAttr
     * @see .absUrl
     */
    open fun attr(attributeKey: String): String {
        if (!hasAttributes()) return EmptyString
        val `val`: String = attributes().getIgnoreCase(attributeKey)
        return if (`val`.length > 0) `val` else if (attributeKey.startsWith("abs:")) absUrl(attributeKey.substring("abs:".length)) else ""
    }

    /**
     * Get all of the element's attributes.
     * @return attributes (which implements iterable, in same order as presented in original HTML).
     */
    abstract fun attributes(): Attributes

    /**
     * Get the number of attributes that this Node has.
     * @return the number of attributes
     * @since 1.14.2
     */
    fun attributesSize(): Int {
        // added so that we can test how many attributes exist without implicitly creating the Attributes object
        return if (hasAttributes()) attributes().size() else 0
    }

    /**
     * Set an attribute (key=value). If the attribute already exists, it is replaced. The attribute key comparison is
     * **case insensitive**. The key will be set with case sensitivity as set in the parser settings.
     * @param attributeKey The attribute key.
     * @param attributeValue The attribute value.
     * @return this (for chaining)
     */
    open fun attr(attributeKey: String, attributeValue: String?): Node {
        var attributeKey = attributeKey
        attributeKey = NodeUtils.parser(this).settings().normalizeAttribute(attributeKey)
        attributes().putIgnoreCase(attributeKey, attributeValue)
        return this
    }

    /**
     * Test if this Node has an attribute. **Case insensitive**.
     * @param attributeKey The attribute key to check.
     * @return true if the attribute exists, false if not.
     */
    open fun hasAttr(attributeKey: String): Boolean {
        if (!hasAttributes()) return false
        if (attributeKey.startsWith("abs:")) {
            val key = attributeKey.substring("abs:".length)
            if (attributes().hasKeyIgnoreCase(key) && !absUrl(key).isEmpty()) return true
        }
        return attributes().hasKeyIgnoreCase(attributeKey)
    }

    /**
     * Remove an attribute from this node.
     * @param attributeKey The attribute to remove.
     * @return this (for chaining)
     */
    open fun removeAttr(attributeKey: String): Node {
        if (hasAttributes()) attributes().removeIgnoreCase(attributeKey)
        return this
    }

    /**
     * Clear (remove) all of the attributes in this node.
     * @return this, for chaining
     */
    open fun clearAttributes(): Node {
        if (hasAttributes()) {
            val it: MutableIterator<Attribute> = attributes().iterator()
            while (it.hasNext()) {
                it.next()
                it.remove()
            }
        }
        return this
    }

    /**
     * Get the base URI that applies to this node. Will return an empty string if not defined. Used to make relative links
     * absolute.
     *
     * @return base URI
     * @see .absUrl
     */
    abstract fun baseUri(): String

    /**
     * Set the baseUri for just this node (not its descendants), if this Node tracks base URIs.
     * @param baseUri new URI
     */
    protected abstract fun doSetBaseUri(baseUri: String?)

    /**
     * Update the base URI of this node and all of its descendants.
     * @param baseUri base URI to set
     */
    fun setBaseUri(baseUri: String?) {
        Validate.notNull(baseUri)
        doSetBaseUri(baseUri)
    }

    /**
     * Get an absolute URL from a URL attribute that may be relative (such as an `<a href>` or
     * `<img src>`).
     *
     *
     * E.g.: `String absUrl = linkEl.absUrl("href");`
     *
     *
     *
     * If the attribute value is already absolute (i.e. it starts with a protocol, like
     * `http://` or `https://` etc), and it successfully parses as a URL, the attribute is
     * returned directly. Otherwise, it is treated as a URL relative to the element's [.baseUri], and made
     * absolute using that.
     *
     *
     *
     * As an alternate, you can use the [.attr] method with the `abs:` prefix, e.g.:
     * `String absUrl = linkEl.attr("abs:href");`
     *
     *
     * @param attributeKey The attribute key
     * @return An absolute URL if one could be made, or an empty string (not null) if the attribute was missing or
     * could not be made successfully into a URL.
     * @see .attr
     *
     * @see java.net.URL.URL
     */
    open fun absUrl(attributeKey: String): String {
        Validate.notEmpty(attributeKey)
        return if (!(hasAttributes() && attributes().hasKeyIgnoreCase(attributeKey))) "" else StringUtil.resolve(
            baseUri(),
            attributes().getIgnoreCase(attributeKey)
        )
    }

    abstract fun ensureChildNodes(): MutableList<Node>

    /**
     * Get a child node by its 0-based index.
     * @param index index of child node
     * @return the child node at this index. Throws a `IndexOutOfBoundsException` if the index is out of bounds.
     */
    fun childNode(index: Int): Node {
        return ensureChildNodes()[index]
    }

    /**
     * Get this node's children. Presented as an unmodifiable list: new children can not be added, but the child nodes
     * themselves can be manipulated.
     * @return list of children. If no children, returns an empty list.
     */
    fun childNodes(): List<Node> {
        if (childNodeSize() == 0) return EmptyNodes
        val children: List<Node> = ensureChildNodes()
        val rewrap: MutableList<Node> =
            ArrayList(children.size) // wrapped so that looping and moving will not throw a CME as the source changes
        rewrap.addAll(children)
        return rewrap
    }

    /**
     * Get the number of child nodes that this node holds.
     * @return the number of child nodes that this node holds.
     */
    abstract fun childNodeSize(): Int
    protected fun childNodesAsArray(): Array<Node> {
        return ensureChildNodes().toTypedArray()
    }

    /**
     * Delete all this node's children.
     * @return this node, for chaining
     */
    abstract fun empty(): Node?

    /**
     * Gets this node's parent node.
     * @return parent node; or null if no parent.
     * @see .hasParent
     */
    open fun parent(): Node? {
        return parentNode
    }

    /**
     * Gets this node's parent node. Not overridable by extending classes, so useful if you really just need the Node type.
     * @return parent node; or null if no parent.
     */
    fun parentNode(): Node? {
        return parentNode
    }

    /**
     * Get this node's root node; that is, its topmost ancestor. If this node is the top ancestor, returns `this`.
     * @return topmost ancestor.
     */
    open fun root(): Node? {
        var node: Node? = this
        while (node!!.parentNode != null) node = node.parentNode
        return node
    }

    /**
     * Gets the Document associated with this Node.
     * @return the Document associated with this Node, or null if there is no such Document.
     */
    fun ownerDocument(): Document? {
        val root = root()
        return if (root is Document) root else null
    }

    /**
     * Remove (delete) this node from the DOM tree. If this node has children, they are also removed.
     */
    fun remove() {
        parentNode!!.removeChild(this)
    }

    /**
     * Insert the specified HTML into the DOM before this node (as a preceding sibling).
     * @param html HTML to add before this node
     * @return this node, for chaining
     * @see .after
     */
    open fun before(html: String): Node {
        addSiblingHtml(siblingIndex, html)
        return this
    }

    /**
     * Insert the specified node into the DOM before this node (as a preceding sibling).
     * @param node to add before this node
     * @return this node, for chaining
     * @see .after
     */
    open fun before(node: Node): Node {
        parentNode!!.addChildren(siblingIndex, node)
        return this
    }

    /**
     * Insert the specified HTML into the DOM after this node (as a following sibling).
     * @param html HTML to add after this node
     * @return this node, for chaining
     * @see .before
     */
    open fun after(html: String): Node {
        addSiblingHtml(siblingIndex + 1, html)
        return this
    }

    /**
     * Insert the specified node into the DOM after this node (as a following sibling).
     * @param node to add after this node
     * @return this node, for chaining
     * @see .before
     */
    open fun after(node: Node): Node {
        parentNode!!.addChildren(siblingIndex + 1, node)
        return this
    }

    private fun addSiblingHtml(index: Int, html: String) {
        val context: Element? = if (parent() is Element) parent() as Element? else null
        val nodes: List<Node> = NodeUtils.parser(this).parseFragmentInput(html, context, baseUri())
        parentNode!!.addChildren(index, *nodes.toTypedArray())
    }

    /**
     * Wrap the supplied HTML around this node.
     *
     * @param html HTML to wrap around this node, e.g. `<div class="head"></div>`. Can be arbitrarily deep. If
     * the input HTML does not parse to a result starting with an Element, this will be a no-op.
     * @return this node, for chaining.
     */
    open fun wrap(html: String): Node {
        Validate.notEmpty(html)

        // Parse context - parent (because wrapping), this, or null
        val context: Element =
            if (parentNode != null && parentNode is Element) parentNode as Element else (if (this is Element) this else null)!!
        val wrapChildren: List<Node> = NodeUtils.parser(this).parseFragmentInput(html, context, baseUri())
        val wrapNode = wrapChildren[0] as? Element // nothing to wrap with; noop
            ?: return this
        val wrap: Element = wrapNode
        val deepest: Element = getDeepChild(wrap)
        if (parentNode != null) parentNode!!.replaceChild(this, wrap)
        deepest.addChildren(this) // side effect of tricking wrapChildren to lose first

        // remainder (unbalanced wrap, like <div></div><p></p> -- The <p> is remainder
        if (wrapChildren.size > 0) {
            for (i in wrapChildren.indices) {
                val remainder = wrapChildren[i]
                // if no parent, this could be the wrap node, so skip
                if (wrap === remainder) continue
                if (remainder.parentNode != null) remainder.parentNode!!.removeChild(remainder)
                wrap.after(remainder)
            }
        }
        return this
    }

    /**
     * Removes this node from the DOM, and moves its children up into the node's parent. This has the effect of dropping
     * the node but keeping its children.
     *
     *
     * For example, with the input html:
     *
     *
     * `<div>One <span>Two <b>Three</b></span></div>`
     * Calling `element.unwrap()` on the `span` element will result in the html:
     *
     * `<div>One Two <b>Three</b></div>`
     * and the `"Two "` [TextNode] being returned.
     *
     * @return the first child of this node, after the node has been unwrapped. @{code Null} if the node had no children.
     * @see .remove
     * @see .wrap
     */
    fun unwrap(): Node? {
        Validate.notNull(parentNode)
        val childNodes: List<Node> = ensureChildNodes()
        val firstChild = if (childNodes.size > 0) childNodes[0] else null
        parentNode!!.addChildren(siblingIndex, *childNodesAsArray())
        this.remove()
        return firstChild
    }

    private fun getDeepChild(el: Element): Element {
        val children: List<Element> = el.children()
        return if (children.size > 0) getDeepChild(children[0]) else el
    }

    open fun nodelistChanged() {
        // Element overrides this to clear its shadow children elements
    }

    /**
     * Replace this node in the DOM with the supplied node.
     * @param in the node that will will replace the existing node.
     */
    fun replaceWith(`in`: Node) {
        Validate.notNull(`in`)
        Validate.notNull(parentNode)
        parentNode!!.replaceChild(this, `in`)
    }

    protected fun replaceChild(out: Node, `in`: Node) {
        Validate.isTrue(out.parentNode === this)
        Validate.notNull(`in`)
        if (`in`.parentNode != null) `in`.parentNode!!.removeChild(`in`)
        val index = out.siblingIndex
        ensureChildNodes()[index] = `in`
        `in`.parentNode = this
        `in`.siblingIndex = index
        out.parentNode = null
    }

    open fun removeChild(out: Node) {
        Validate.isTrue(out.parentNode === this)
        val index = out.siblingIndex
        ensureChildNodes().removeAt(index)
        reindexChildren(index)
        out.parentNode = null
    }

    protected fun addChildren(vararg children: Node) {
        //most used. short circuit addChildren(int), which hits reindex children and array copy
        val nodes = ensureChildNodes()
        for (child in children) {
            reparentChild(child)
            nodes.add(child)
            child.siblingIndex = nodes.size - 1
        }
    }

    fun addChildren(index: Int, vararg children: Node) {
        if (children.isEmpty()) {
            return
        }
        val nodes = ensureChildNodes()

        // fast path - if used as a wrap (index=0, children = child[0].parent.children - do inplace
        val firstParent = children[0].parent()
        if (firstParent != null && firstParent.childNodeSize() == children.size) {
            var sameList = true
            val firstParentNodes: List<Node> = firstParent.ensureChildNodes()
            // identity check contents to see if same
            var i = children.size
            while (i-- > 0) {
                if (children[i] !== firstParentNodes[i]) {
                    sameList = false
                    break
                }
            }
            if (sameList) { // moving, so OK to empty firstParent and short-circuit
                val wasEmpty = childNodeSize() == 0
                firstParent.empty()
                nodes.addAll(index, children.toList())
                i = children.size
                while (i-- > 0) {
                    children[i].parentNode = this
                }
                if (!(wasEmpty && children[0].siblingIndex == 0)) // skip reindexing if we just moved
                    reindexChildren(index)
                return
            }
        }
        for (child in children) {
            reparentChild(child)
        }
        nodes.addAll(index, children.toList())
        reindexChildren(index)
    }

    protected fun reparentChild(child: Node) {
        child.parentNode = this
    }

    private fun reindexChildren(start: Int) {
        if (childNodeSize() == 0) return
        val childNodes: List<Node> = ensureChildNodes()
        for (i in start until childNodes.size) {
            childNodes[i].siblingIndex = i
        }
    }

    /**
     * Retrieves this node's sibling nodes. Similar to [node.parent.childNodes()][.childNodes], but does not
     * include this node (a node is not a sibling of itself).
     * @return node siblings. If the node has no parent, returns an empty list.
     */
    fun siblingNodes(): List<Node> {
        if (parentNode == null) return emptyList()
        val nodes: List<Node> = parentNode!!.ensureChildNodes()
        val siblings: MutableList<Node> = ArrayList(nodes.size - 1)
        for (node in nodes) if (node !== this) siblings.add(node)
        return siblings
    }

    /**
     * Get this node's next sibling.
     * @return next sibling, or @{code null} if this is the last sibling
     */
    fun nextSibling(): Node? {
        if (parentNode == null) return null // root
        val siblings: List<Node> = parentNode!!.ensureChildNodes()
        val index = siblingIndex + 1
        return if (siblings.size > index) siblings[index] else null
    }

    /**
     * Get this node's previous sibling.
     * @return the previous sibling, or @{code null} if this is the first sibling
     */
    fun previousSibling(): Node? {
        if (parentNode == null) return null // root
        return if (siblingIndex > 0) parentNode!!.ensureChildNodes()[siblingIndex - 1] else null
    }

    /**
     * Perform a depth-first traversal through this node and its descendants.
     * @param nodeVisitor the visitor callbacks to perform on each node
     * @return this node, for chaining
     */
    open fun traverse(nodeVisitor: NodeVisitor): Node {
        NodeTraversor.traverse(nodeVisitor, this)
        return this
    }

    /**
     * Perform the supplied action on this Node and each of its descendants, during a depth-first traversal. Nodes may be
     * inspected, changed, added, replaced, or removed.
     * @param action the function to perform on the node
     * @return this Node, for chaining
     * @see Element.forEach
     */
    open fun forEachNode(action: (Node)->Unit): Node {
        NodeTraversor.traverse(object :NodeVisitor {
            override fun head(node: Node, depth: Int) {
                action(node)
            }
        }, this)
        return this
    }

    /**
     * Perform a depth-first filtering through this node and its descendants.
     * @param nodeFilter the filter callbacks to perform on each node
     * @return this node, for chaining
     */
    open fun filter(nodeFilter: NodeFilter): Node {
        NodeTraversor.filter(nodeFilter, this)
        return this
    }

    /**
     * Get the outer HTML of this node. For example, on a `p` element, may return `<p>Para</p>`.
     * @return outer HTML
     * @see Element.html
     * @see Element.text
     */
    open fun outerHtml(): String {
        val accum: StringBuilder = StringUtil.borrowBuilder()
        outerHtml(accum)
        return StringUtil.releaseBuilder(accum)
    }

    fun outerHtml(accum: Appendable) {
        NodeTraversor.traverse(OuterHtmlVisitor(accum, NodeUtils.outputSettings(this)), this)
    }

    /**
     * Get the outer HTML of this node.
     * @param accum accumulator to place HTML into
     * @throws IOException if appending to the given accumulator fails.
     */
    @Throws(IOException::class)
    abstract fun outerHtmlHead(accum: Appendable, depth: Int, out: OutputSettings)
    @Throws(IOException::class)
    abstract fun outerHtmlTail(accum: Appendable, depth: Int, out: OutputSettings)

    /**
     * Write this node and its children to the given [Appendable].
     *
     * @param appendable the [Appendable] to write to.
     * @return the supplied [Appendable], for chaining.
     */
    open fun <T : Appendable> html(appendable: T): T {
        outerHtml(appendable)
        return appendable
    }

    /**
     * Gets this node's outer HTML.
     * @return outer HTML.
     * @see .outerHtml
     */
    override fun toString(): String {
        return outerHtml()
    }

    @Throws(IOException::class)
    protected fun indent(accum: Appendable, depth: Int, out: OutputSettings) {
        accum.append('\n').append(StringUtil.padding(depth * out.indentAmount(), out.maxPaddingWidth()))
    }

    /**
     * Check if this node is the same instance of another (object identity test).
     *
     * For an node value equality check, see [.hasSameValue]
     * @param o other object to compare to
     * @return true if the content of this node is the same as the other
     * @see Node.hasSameValue
     */
    override fun equals(o: Any?): Boolean {
        // implemented just so that javadoc is clear this is an identity test
        return this === o
    }

    /**
     * Provides a hashCode for this Node, based on it's object identity. Changes to the Node's content will not impact the
     * result.
     * @return an object identity based hashcode for this Node
     */
    override fun hashCode(): Int {
        // implemented so that javadoc and scanners are clear this is an identity test
        return super.hashCode()
    }

    /**
     * Check if this node is has the same content as another node. A node is considered the same if its name, attributes and content match the
     * other node; particularly its position in the tree does not influence its similarity.
     * @param o other object to compare to
     * @return true if the content of this node is the same as the other
     */
    fun hasSameValue(o: Any?): Boolean {
        if (this === o) return true
        return if (o == null || this::class != o::class) false else this.outerHtml() == (o as Node).outerHtml()
    }

    private class OuterHtmlVisitor internal constructor(private val accum: Appendable, out: OutputSettings) :
        NodeVisitor {

        private val out: OutputSettings

        init {
            this.out = out
            out.prepareEncoder()
        }

        override fun head(node: Node, depth: Int) {
            try {
                node.outerHtmlHead(accum, depth, out)
            } catch (exception: IOException) {
                throw SerializationException(exception)
            }
        }

        override fun tail(node: Node, depth: Int) {
            if (node.nodeName() != "#text") { // saves a void hit.
                try {
                    node.outerHtmlTail(accum, depth, out)
                } catch (exception: IOException) {
                    throw SerializationException(exception)
                }
            }
        }
    }

    companion object {

        val EmptyNodes: MutableList<Node> = mutableListOf()
        const val EmptyString = ""
    }
}