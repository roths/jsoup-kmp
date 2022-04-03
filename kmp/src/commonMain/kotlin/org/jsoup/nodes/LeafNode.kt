package org.jsoup.nodes

import org.jsoup.helper.Validate

abstract class LeafNode : Node() {

    var value // either a string value, or an attribute map (in the rare case multiple attributes are set)
            : Any? = null

    protected override fun hasAttributes(): Boolean {
        return value is Attributes
    }

    override fun attributes(): Attributes {
        ensureAttributes()
        return value as Attributes
    }

    private fun ensureAttributes() {
        if (!hasAttributes()) {
            val coreValue = value
            val attributes = Attributes()
            value = attributes
            if (coreValue != null) attributes.put(nodeName(), coreValue as String?)
        }
    }

    fun coreValue(): String {
        return attr(nodeName())
    }

    fun coreValue(value: String?) {
        attr(nodeName(), value)
    }

    override fun attr(key: String): String {
        return if (!hasAttributes()) {
            if (nodeName().equals(key)) value as String else EmptyString
        } else super.attr(key)
    }

    override fun attr(key: String, value: String?): Node {
        if (!hasAttributes() && key == nodeName()) {
            this.value = value
        } else {
            ensureAttributes()
            super.attr(key, value)
        }
        return this
    }

    override fun hasAttr(key: String): Boolean {
        ensureAttributes()
        return super.hasAttr(key)
    }

    override fun removeAttr(key: String): Node {
        ensureAttributes()
        return super.removeAttr(key)
    }

    override fun absUrl(key: String): String {
        ensureAttributes()
        return super.absUrl(key)
    }

    override fun baseUri(): String {
        return if (hasParent()) parent()!!.baseUri() else ""
    }

    protected override fun doSetBaseUri(baseUri: String?) {
        // noop
    }

    override fun childNodeSize(): Int {
        return 0
    }

    override fun empty(): Node {
        return this
    }

    override fun ensureChildNodes(): MutableList<Node> {
        return EmptyNodes
    }
}