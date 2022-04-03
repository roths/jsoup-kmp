package org.jsoup.parser

import org.jsoup.helper.Validate
import org.jsoup.nodes.Attributes
import org.jsoup.parser.Token.TokenType.Character
import org.jsoup.parser.Token.TokenType.Comment
import org.jsoup.parser.Token.TokenType.Doctype
import org.jsoup.parser.Token.TokenType.EOF
import org.jsoup.parser.Token.TokenType.EndTag
import org.jsoup.parser.Token.TokenType.StartTag
import kotlinx.*

/**
 * Parse tokens for the Tokeniser.
 */
abstract class Token private constructor() {

    var type: TokenType? = null
    fun tokenType(): String {
        return this::class.simpleName.toString()
    }

    /**
     * Reset the data represent by this token, for reuse. Prevents the need to create transfer objects for every
     * piece of data, which immediately get GCed.
     */
    abstract fun reset(): Token
    class Doctype : Token() {

        val name = StringBuilder()
        var pubSysKey: String? = null
        val publicIdentifier = StringBuilder()
        val systemIdentifier = StringBuilder()
        var forceQuirks = false
        override fun reset(): Token {
            reset(name)
            pubSysKey = null
            reset(publicIdentifier)
            reset(systemIdentifier)
            forceQuirks = false
            return this
        }

        fun getName(): String {
            return name.toString()
        }

        fun getPublicIdentifier(): String {
            return publicIdentifier.toString()
        }

        fun getSystemIdentifier(): String {
            return systemIdentifier.toString()
        }

        fun isForceQuirks(): Boolean {
            return forceQuirks
        }

        override fun toString(): String {
            return "<!doctype " + getName() + ">"
        }

        init {
            type = Doctype
        }
    }

    abstract class Tag : Token() {

        var tagName: String? = null

        var normalName // lc version of tag name, for case insensitive tree build
                : String? = null
        private val attrName = StringBuilder() // try to get attr names and vals in one shot, vs Builder

        private var attrNameS: String? = null
        private var hasAttrName = false
        private val attrValue = StringBuilder()

        private var attrValueS: String? = null
        private var hasAttrValue = false
        private var hasEmptyAttrValue = false // distinguish boolean attribute from empty string value
        var selfClosing = false

        var attributes // start tags get attributes on construction. End tags get attributes on first new attribute (but only for parser convenience, not used).
                : Attributes? = null

        override fun reset(): Tag {
            tagName = null
            normalName = null
            reset(attrName)
            attrNameS = null
            hasAttrName = false
            reset(attrValue)
            attrValueS = null
            hasEmptyAttrValue = false
            hasAttrValue = false
            selfClosing = false
            attributes = null
            return this
        }

        fun newAttribute() {
            if (attributes == null) {
                attributes = Attributes()
            }
            if (hasAttrName && attributes!!.size() < MaxAttributes) {
                // the tokeniser has skipped whitespace control chars, but trimming could collapse to empty for other control codes, so verify here
                var name = if (attrName.length > 0) attrName.toString() else attrNameS!!
                name = name.trim { it <= ' ' }
                if (name.length > 0) {
                    val value: String?
                    value =
                        if (hasAttrValue) if (attrValue.length > 0) attrValue.toString() else attrValueS else if (hasEmptyAttrValue) "" else null
                    // note that we add, not put. So that the first is kept, and rest are deduped, once in a context where case sensitivity is known (the appropriate tree builder).
                    attributes!!.add(name, value)
                }
            }
            reset(attrName)
            attrNameS = null
            hasAttrName = false
            reset(attrValue)
            attrValueS = null
            hasAttrValue = false
            hasEmptyAttrValue = false
        }

        fun hasAttributes(): Boolean {
            return attributes != null
        }

        fun hasAttribute(key: String?): Boolean {
            return attributes != null && attributes!!.hasKey(key)
        }

        fun finaliseTag() {
            // finalises for emit
            if (hasAttrName) {
                newAttribute()
            }
        }

        /** Preserves case  */
        fun name(): String? { // preserves case, for input into Tag.valueOf (which may drop case)
            Validate.isFalse(tagName == null || tagName!!.length == 0)
            return tagName
        }

        /** Lower case  */
        fun normalName(): String? { // lower case, used in tree building for working out where in tree it should go
            return normalName
        }

        fun toStringName(): String {
            return if (tagName != null) tagName!! else "[unset]"
        }

        fun name(name: String): Tag {
            tagName = name
            normalName = ParseSettings.normalName(name)
            return this
        }

        fun isSelfClosing(): Boolean {
            return selfClosing
        }

        // these appenders are rarely hit in not null state-- caused by null chars.
        fun appendTagName(append: String) {
            // might have null chars - need to replace with null replacement character
            var append = append
            append = append.replace(TokeniserState.nullChar, Tokeniser.replacementChar)
            tagName = if (tagName == null) {
                append
            } else {
                tagName + append
            }
            normalName = ParseSettings.normalName(tagName!!)
        }

        fun appendTagName(append: Char) {
            appendTagName(append.toString())
        }

        fun appendAttributeName(append: String) {
            // might have null chars because we eat in one pass - need to replace with null replacement character
            var append = append
            append = append.replace(TokeniserState.nullChar, Tokeniser.replacementChar)
            ensureAttrName()
            if (attrName.length == 0) {
                attrNameS = append
            } else {
                attrName.append(append)
            }
        }

        fun appendAttributeName(append: Char) {
            ensureAttrName()
            attrName.append(append)
        }

        fun appendAttributeValue(append: String?) {
            ensureAttrValue()
            if (attrValue.length == 0) {
                attrValueS = append
            } else {
                attrValue.append(append)
            }
        }

        fun appendAttributeValue(append: Char) {
            ensureAttrValue()
            attrValue.append(append)
        }

        fun appendAttributeValue(append: CharArray?) {
            ensureAttrValue()
            attrValue.append(append)
        }

        fun appendAttributeValue(appendCodepoints: IntArray) {
            ensureAttrValue()
            for (codepoint in appendCodepoints) {
                attrValue.appendCodePoint(codepoint)
            }
        }

        fun setEmptyAttributeValue() {
            hasEmptyAttrValue = true
        }

        private fun ensureAttrName() {
            hasAttrName = true
            // if on second hit, we'll need to move to the builder
            if (attrNameS != null) {
                attrName.append(attrNameS)
                attrNameS = null
            }
        }

        private fun ensureAttrValue() {
            hasAttrValue = true
            // if on second hit, we'll need to move to the builder
            if (attrValueS != null) {
                attrValue.append(attrValueS)
                attrValueS = null
            }
        }

        abstract override fun toString(): String

        companion object {

            /* Limits runaway crafted HTML from spewing attributes and getting a little sluggish in ensureCapacity.
        Real-world HTML will P99 around 8 attributes, so plenty of headroom. Implemented here and not in the Attributes
        object so that API users can add more if ever required. */
            private const val MaxAttributes = 512
        }
    }

    class StartTag : Tag() {

        override fun reset(): Tag {
            super.reset()
            attributes = null
            return this
        }

        fun nameAttr(name: String, attributes: Attributes?): StartTag {
            tagName = name
            this.attributes = attributes
            normalName = ParseSettings.normalName(name)
            return this
        }

        override fun toString(): String {
            return if (hasAttributes() && attributes!!.size() > 0) {
                "<" + toStringName() + " " + attributes.toString() + ">"
            } else {
                "<" + toStringName() + ">"
            }
        }

        init {
            type = StartTag
        }
    }

    class EndTag : Tag() {

        override fun toString(): String {
            return "</" + toStringName() + ">"
        }

        init {
            type = EndTag
        }
    }

    class Comment : Token() {

        private val data = StringBuilder()
        private var dataS // try to get in one shot
                : String? = null
        var bogus = false
        override fun reset(): Token {
            reset(data)
            dataS = null
            bogus = false
            return this
        }

        fun getData(): String {
            return if (dataS != null) dataS!! else data.toString()
        }

        fun append(append: String?): Comment {
            ensureData()
            if (data.length == 0) {
                dataS = append
            } else {
                data.append(append)
            }
            return this
        }

        fun append(append: Char): Comment {
            ensureData()
            data.append(append)
            return this
        }

        private fun ensureData() {
            // if on second hit, we'll need to move to the builder
            if (dataS != null) {
                data.append(dataS)
                dataS = null
            }
        }

        override fun toString(): String {
            return "<!--" + getData() + "-->"
        }

        init {
            type = Comment
        }
    }

    open class Character : Token() {

        private var data: String? = null
        override fun reset(): Token {
            data = null
            return this
        }

        fun data(data: String?): Character {
            this.data = data
            return this
        }

        fun getData(): String? {
            return data
        }

        override fun toString(): String {
            return getData().toString()
        }

        init {
            type = Character
        }
    }

    internal class CData(data: String?) : Character() {

        override fun toString(): String {
            return "<![CDATA[" + getData().toString() + "]]>"
        }

        init {
            data(data)
        }
    }

    internal class EOF : Token() {

        override fun reset(): Token {
            return this
        }

        override fun toString(): String {
            return ""
        }

        init {
            type = EOF
        }
    }

    fun isDoctype(): Boolean {
        return type == Doctype
    }

    fun asDoctype(): Doctype {
        return this as Doctype
    }

    fun isStartTag(): Boolean {
        return type == StartTag
    }

    fun asStartTag(): StartTag {
        return this as StartTag
    }

    fun isEndTag(): Boolean {
        return type == EndTag
    }

    fun asEndTag(): EndTag {
        return this as EndTag
    }

    fun isComment(): Boolean {
        return type == Comment
    }

    fun asComment(): Comment {
        return this as Comment
    }

    fun isCharacter(): Boolean {
        return type == Character
    }

    fun isCData(): Boolean {
        return this is CData
    }

    fun asCharacter(): Character {
        return this as Character
    }

    fun isEOF(): Boolean {
        return type == EOF
    }

    enum class TokenType {
        Doctype, StartTag, EndTag, Comment, Character,  // note no CData - treated in builder as an extension of Character
        EOF
    }

    companion object {

        fun reset(sb: StringBuilder?) {
            sb?.clear()
        }
    }
}