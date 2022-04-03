package org.jsoup.parser

import org.jsoup.helper.Validate

/**
 * HTML Tag capabilities.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
class Tag private constructor(private var tagName: String) {

    private val normalName // always the lower case version of this tag, regardless of case preservation mode
            : String
    private var isBlock = true // block
    private var formatAsBlock = true // should be formatted as a block
    private var empty = false // can hold nothing; e.g. img
    private var selfClosing = false // can self close (<foo />). used for unknown tags that self close, without forcing them as empty.
    private var preserveWhitespace = false // for pre, textarea, script etc
    private var formList = false // a control that appears in forms: input, textarea, output etc
    private var formSubmit = false // a control that can be submitted in a form: input etc

    init {
        normalName = tagName.lowercase()
    }
    /**
     * Get this tag's name.
     *
     * @return the tag's name
     */
    fun getName(): String {
        return tagName
    }

    /**
     * Get this tag's normalized (lowercased) name.
     * @return the tag's normal name.
     */
    fun normalName(): String {
        return normalName
    }

    /**
     * Gets if this is a block tag.
     *
     * @return if block tag
     */
    fun isBlock(): Boolean {
        return isBlock
    }

    /**
     * Gets if this tag should be formatted as a block (or as inline)
     *
     * @return if should be formatted as block or inline
     */
    fun formatAsBlock(): Boolean {
        return formatAsBlock
    }

    /**
     * Gets if this tag is an inline tag.
     *
     * @return if this tag is an inline tag.
     */
    fun isInline(): Boolean {
        return !isBlock
    }

    /**
     * Get if this is an empty tag
     *
     * @return if this is an empty tag
     */
    fun isEmpty(): Boolean {
        return empty
    }

    /**
     * Get if this tag is self closing.
     *
     * @return if this tag should be output as self closing.
     */
    fun isSelfClosing(): Boolean {
        return empty || selfClosing
    }

    /**
     * Get if this is a pre-defined tag, or was auto created on parsing.
     *
     * @return if a known tag
     */
    fun isKnownTag(): Boolean {
        return tags.containsKey(tagName)
    }

    /**
     * Get if this tag should preserve whitespace within child text nodes.
     *
     * @return if preserve whitespace
     */
    fun preserveWhitespace(): Boolean {
        return preserveWhitespace
    }

    /**
     * Get if this tag represents a control associated with a form. E.g. input, textarea, output
     * @return if associated with a form
     */
    fun isFormListed(): Boolean {
        return formList
    }

    /**
     * Get if this tag represents an element that should be submitted with a form. E.g. input, option
     * @return if submittable with a form
     */
    fun isFormSubmittable(): Boolean {
        return formSubmit
    }

    fun setSelfClosing(): Tag {
        selfClosing = true
        return this
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is Tag) return false
        val tag = o
        if (tagName != tag.tagName) return false
        if (empty != tag.empty) return false
        if (formatAsBlock != tag.formatAsBlock) return false
        if (isBlock != tag.isBlock) return false
        if (preserveWhitespace != tag.preserveWhitespace) return false
        if (selfClosing != tag.selfClosing) return false
        return if (formList != tag.formList) false else formSubmit == tag.formSubmit
    }

    override fun hashCode(): Int {
        var result = tagName.hashCode()
        result = 31 * result + if (isBlock) 1 else 0
        result = 31 * result + if (formatAsBlock) 1 else 0
        result = 31 * result + if (empty) 1 else 0
        result = 31 * result + if (selfClosing) 1 else 0
        result = 31 * result + if (preserveWhitespace) 1 else 0
        result = 31 * result + if (formList) 1 else 0
        result = 31 * result + if (formSubmit) 1 else 0
        return result
    }

    override fun toString(): String {
        return tagName
    }

    protected fun clone(): Tag {
        return Tag(tagName).also {
            it.isBlock = this.isBlock
            it.formatAsBlock = this.formatAsBlock
            it.empty = this.empty
            it.selfClosing = this.selfClosing
            it.preserveWhitespace = this.preserveWhitespace
            it.formList = this.formList
            it.formSubmit = this.formSubmit
        }
    }

    companion object {

        private val tags: MutableMap<String, Tag> = HashMap() // map of known tags
        /**
         * Get a Tag by name. If not previously defined (unknown), returns a new generic tag, that can do anything.
         *
         *
         * Pre-defined tags (P, DIV etc) will be ==, but unknown tags are not registered and will only .equals().
         *
         *
         * @param tagName Name of tag, e.g. "p". Case insensitive.
         * @param settings used to control tag name sensitivity
         * @return The tag, either defined or new generic.
         */
        /**
         * Get a Tag by name. If not previously defined (unknown), returns a new generic tag, that can do anything.
         *
         *
         * Pre-defined tags (P, DIV etc) will be ==, but unknown tags are not registered and will only .equals().
         *
         *
         * @param tagName Name of tag, e.g. "p". **Case sensitive**.
         * @return The tag, either defined or new generic.
         */
        fun valueOf(tagName: String, settings: ParseSettings = ParseSettings.preserveCase): Tag {
            var tagName = tagName
            Validate.notNull(tagName)
            var tag = tags[tagName]
            if (tag == null) {
                tagName = settings.normalizeTag(tagName) // the name we'll use
                Validate.notEmpty(tagName)
                val normalName: String = tagName.lowercase() // the lower-case name to get tag settings off
                tag = tags[normalName]
                if (tag == null) {
                    // not defined: create default; go anywhere, do anything! (incl be inside a <p>)
                    tag = Tag(tagName)
                    tag.isBlock = false
                } else if (settings.preserveTagCase() && tagName != normalName) {
                    tag = tag.clone() // get a new version vs the static one, so name update doesn't reset all
                    tag.tagName = tagName
                }
            }
            return tag
        }

        /**
         * Check if this tagname is a known tag.
         *
         * @param tagName name of tag
         * @return if known HTML tag
         */
        fun isKnownTag(tagName: String): Boolean {
            return tags.containsKey(tagName)
        }

        // internal static initialisers:
        // prepped from http://www.w3.org/TR/REC-html40/sgml/dtd.html and other sources
        private val blockTags = arrayOf(
            "html",
            "head",
            "body",
            "frameset",
            "script",
            "noscript",
            "style",
            "meta",
            "link",
            "title",
            "frame",
            "noframes",
            "section",
            "nav",
            "aside",
            "hgroup",
            "header",
            "footer",
            "p",
            "h1",
            "h2",
            "h3",
            "h4",
            "h5",
            "h6",
            "ul",
            "ol",
            "pre",
            "div",
            "blockquote",
            "hr",
            "address",
            "figure",
            "figcaption",
            "form",
            "fieldset",
            "ins",
            "del",
            "dl",
            "dt",
            "dd",
            "li",
            "table",
            "caption",
            "thead",
            "tfoot",
            "tbody",
            "colgroup",
            "col",
            "tr",
            "th",
            "td",
            "video",
            "audio",
            "canvas",
            "details",
            "menu",
            "plaintext",
            "template",
            "article",
            "main",
            "svg",
            "math",
            "center",
            "template",
            "dir",
            "applet",
            "marquee",
            "listing" // deprecated but still known / special handling
        )
        private val inlineTags = arrayOf(
            "object",
            "base",
            "font",
            "tt",
            "i",
            "b",
            "u",
            "big",
            "small",
            "em",
            "strong",
            "dfn",
            "code",
            "samp",
            "kbd",
            "var",
            "cite",
            "abbr",
            "time",
            "acronym",
            "mark",
            "ruby",
            "rt",
            "rp",
            "a",
            "img",
            "br",
            "wbr",
            "map",
            "q",
            "sub",
            "sup",
            "bdo",
            "iframe",
            "embed",
            "span",
            "input",
            "select",
            "textarea",
            "label",
            "button",
            "optgroup",
            "option",
            "legend",
            "datalist",
            "keygen",
            "output",
            "progress",
            "meter",
            "area",
            "param",
            "source",
            "track",
            "summary",
            "command",
            "device",
            "area",
            "basefont",
            "bgsound",
            "menuitem",
            "param",
            "source",
            "track",
            "data",
            "bdi",
            "s",
            "strike",
            "nobr"
        )
        private val emptyTags = arrayOf(
            "meta", "link", "base", "frame", "img", "br", "wbr", "embed", "hr", "input", "keygen", "col", "command",
            "device", "area", "basefont", "bgsound", "menuitem", "param", "source", "track"
        )

        // todo - rework this to format contents as inline; and update html emitter in Element. Same output, just neater.
        private val formatAsInlineTags = arrayOf(
            "title",
            "a",
            "p",
            "h1",
            "h2",
            "h3",
            "h4",
            "h5",
            "h6",
            "pre",
            "address",
            "li",
            "th",
            "td",
            "script",
            "style",
            "ins",
            "del",
            "s"
        )
        private val preserveWhitespaceTags = arrayOf(
            "pre",
            "plaintext",
            "title",
            "textarea" // script is not here as it is a data node, which always preserve whitespace
        )

        // todo: I think we just need submit tags, and can scrub listed
        private val formListedTags = arrayOf(
            "button", "fieldset", "input", "keygen", "object", "output", "select", "textarea"
        )
        private val formSubmitTags = arrayOf(
            "input", "keygen", "object", "select", "textarea"
        )

        private fun register(tag: Tag) {
            tags[tag.tagName] = tag
        }

        init {
            // creates
            for (tagName in blockTags) {
                val tag = Tag(tagName)
                register(tag)
            }
            for (tagName in inlineTags) {
                val tag = Tag(tagName)
                tag.isBlock = false
                tag.formatAsBlock = false
                register(tag)
            }

            // mods:
            for (tagName in emptyTags) {
                val tag = tags[tagName]!!
                tag.empty = true
            }
            for (tagName in formatAsInlineTags) {
                val tag = tags[tagName]!!
                tag.formatAsBlock = false
            }
            for (tagName in preserveWhitespaceTags) {
                val tag = tags[tagName]!!
                tag.preserveWhitespace = true
            }
            for (tagName in formListedTags) {
                val tag = tags[tagName]!!
                tag.formList = true
            }
            for (tagName in formSubmitTags) {
                val tag = tags[tagName]!!
                tag.formSubmit = true
            }
        }
    }
}