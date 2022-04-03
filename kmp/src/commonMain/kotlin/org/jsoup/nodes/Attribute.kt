package org.jsoup.nodes

import kotlinx.io.IOException
import org.jsoup.SerializationException
import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Document.OutputSettings
import org.jsoup.nodes.Document.OutputSettings.Syntax

/**
 * A single key + value attribute. (Only used for presentation.)
 */
class Attribute(key: String, `val`: String?, parent: Attributes?) : MutableMap.MutableEntry<String, String?> {

    override var key: String = ""
        get() = field
        set(value) {
            var key = value
            key = key.trim { it <= ' ' }
            // trimming could potentially make empty, so validate here
            Validate.notEmpty(key)
            parent?.also {
                val i = it.indexOfKey(this.key)
                if (i != Attributes.NotFound) {
                    it.keys[i] = key
                }
            }
            field = key
        }

    override val value: String
        get() = valueInner
    private var valueInner: String = ""

    override fun setValue(newValue: String?): String {
        var oldVal = this.value
        parent?.let {
            val i = it.indexOfKey(key)
            if (i != Attributes.NotFound) {
                oldVal = it.get(key) // trust the container more
                // corresponding to jsoup, set null if attr is empty
                it.vals[i] = newValue
            }
        }
        this.valueInner = Attributes.checkNotNull(newValue)
        return Attributes.checkNotNull(oldVal)
    }

    var parent // used to update the holding Attributes when the key / value is changed via this interface
            : Attributes?


    /**
     * Create a new attribute from unencoded (raw) key and value.
     * @param key attribute key; case is preserved.
     * @param val attribute value (may be null)
     * @param parent the containing Attributes (this Attribute is not automatically added to said Attributes)
     * @see .createFromEncoded
     */
    init {
        this.key = key
        this.valueInner = Attributes.checkNotNull(`val`)
        this.parent = parent
    }
    /**
     * Create a new attribute from unencoded (raw) key and value.
     * @param key attribute key; case is preserved.
     * @param value attribute value (may be null)
     * @see .createFromEncoded
     */
    constructor(key: String, value: String?) : this(key, value, null)

    /**
     * Check if this Attribute has a value. Set boolean attributes have no value.
     * @return if this is a boolean attribute / attribute without a value
     */
    fun hasDeclaredValue(): Boolean {
        return value != null
    }

    /**
     * Get the HTML representation of this attribute; e.g. `href="index.html"`.
     * @return HTML
     */
    fun html(): String {
        val sb: StringBuilder = StringUtil.borrowBuilder()
        try {
            html(sb, Document("").outputSettings())
        } catch (exception: IOException) {
            throw SerializationException(exception)
        }
        return StringUtil.releaseBuilder(sb)
    }

    @Throws(IOException::class)
    protected fun html(accum: Appendable, out: OutputSettings) {
        html(key, value, accum, out)
    }

    /**
     * Get the string representation of this attribute, implemented as [.html].
     * @return string
     */
    override fun toString(): String {
        return html()
    }

    fun isDataAttribute(): Boolean {
        return isDataAttribute(key)
    }

    /**
     * Collapsible if it's a boolean attribute and value is empty or same as name
     *
     * @param out output settings
     * @return  Returns whether collapsible or not
     */
    protected fun shouldCollapseAttribute(out: OutputSettings): Boolean {
        return shouldCollapseAttribute(key, value, out)
    }

    override fun equals(o: Any?): Boolean { // note parent not considered
        if (this === o) return true
        if (o == null || this::class != o::class) return false
        val attribute = o as Attribute
        if (key != attribute.key) return false
        return if (value != null) value == attribute.value else attribute.value == null
    }

    override fun hashCode(): Int { // note parent not considered
        var result = key.hashCode()
        result = 31 * result + if (value != null) value.hashCode() else 0
        return result
    }

    companion object {

        private val booleanAttributes = arrayOf(
            "allowfullscreen", "async", "autofocus", "checked", "compact", "declare", "default", "defer", "disabled",
            "formnovalidate", "hidden", "inert", "ismap", "itemscope", "multiple", "muted", "nohref", "noresize",
            "noshade", "novalidate", "nowrap", "open", "readonly", "required", "reversed", "seamless", "selected",
            "sortable", "truespeed", "typemustmatch"
        )

        @Throws(IOException::class)
        protected fun html(key: String?, `val`: String?, accum: Appendable, out: OutputSettings) {
            var key = key
            key = getValidKey(key, out.syntax())
            if (key == null) return  // can't write it :(
            htmlNoValidate(key, `val`, accum, out)
        }

        @Throws(IOException::class)
        fun htmlNoValidate(key: String, `val`: String?, accum: Appendable, out: OutputSettings) {
            // structured like this so that Attributes can check we can write first, so it can add whitespace correctly
            accum.append(key)
            if (!shouldCollapseAttribute(key, `val`, out)) {
                accum.append("=\"")
                Entities.escape(accum, Attributes.checkNotNull(`val`), out, true, false, false)
                accum.append('"')
            }
        }

        private val xmlKeyValid = """[a-zA-Z_:][-a-zA-Z0-9_:.]*""".toRegex()
        private val xmlKeyReplace = """[^-a-zA-Z0-9_:.]""".toRegex()
        private val htmlKeyValid = """[^\x00-\x1f\x7f-\x9f "'/=]+""".toRegex()
        private val htmlKeyReplace = """[\x00-\x1f\x7f-\x9f "'/=]""".toRegex()

        fun getValidKey(key: String?, syntax: Syntax): String? {
            // we consider HTML attributes to always be valid. XML checks key validity
            var key = key
            if (syntax === Syntax.xml && key != null && !xmlKeyValid.matches(key)) {
                key = xmlKeyReplace.replace(key, "")
                return if (xmlKeyValid.matches(key)) {
                    key
                } else {
                    null // null if could not be coerced
                }
            } else if (syntax === Syntax.html && key!=null && !htmlKeyValid.matches(key)) {
                key = htmlKeyReplace.replace(key, "")
                return if (htmlKeyValid.matches(key)) {
                    key
                } else {
                    null // null if could not be coerced
                }
            }
            return key
        }

        /**
         * Create a new Attribute from an unencoded key and a HTML attribute encoded value.
         * @param unencodedKey assumes the key is not encoded, as can be only run of simple \w chars.
         * @param encodedValue HTML attribute encoded value
         * @return attribute
         */
        fun createFromEncoded(unencodedKey: String, encodedValue: String): Attribute {
            val value: String = Entities.unescape(encodedValue, true)
            return Attribute(unencodedKey, value, null) // parent will get set when Put
        }

        protected fun isDataAttribute(key: String): Boolean {
            return key.startsWith(Attributes.dataPrefix) && key.length > Attributes.dataPrefix.length
        }

        // collapse unknown foo=null, known checked=null, checked="", checked=checked; write out others
        protected fun shouldCollapseAttribute(key: String, `val`: String?, out: OutputSettings): Boolean {
            return out.syntax() === Syntax.html && (`val` == null || (`val`.isEmpty() || `val`.equals(key, ignoreCase = true)) && isBooleanAttribute(key))
        }

        /**
         * Checks if this attribute name is defined as a boolean attribute in HTML5
         */
        fun isBooleanAttribute(key: String): Boolean {
            return booleanAttributes.asList().binarySearch(key.lowercase()) >= 0
        }
    }
}