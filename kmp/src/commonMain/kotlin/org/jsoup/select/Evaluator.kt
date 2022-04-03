package org.jsoup.select

import org.jsoup.helper.Validate
import org.jsoup.internal.Normalizer.normalize
import org.jsoup.internal.StringUtil.normaliseWhitespace
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.DocumentType
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.PseudoTextElement
import org.jsoup.nodes.TextNode
import org.jsoup.nodes.XmlDeclaration

/**
 * Evaluates that an element matches the selector.
 */
abstract class Evaluator protected constructor() {

    /**
     * Test if the element meets the evaluator's requirements.
     *
     * @param root    Root of the matching subtree
     * @param element tested element
     * @return Returns <tt>true</tt> if the requirements are met or
     * <tt>false</tt> otherwise
     */
    abstract fun matches(root: Element?, element: Element): Boolean

    /**
     * Evaluator for tag name
     */
    class Tag(private val tagName: String) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.normalName().equals(tagName)
        }

        override fun toString(): String {
            return tagName
        }
    }

    /**
     * Evaluator for tag name that ends with
     */
    class TagEndsWith(private val tagName: String) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.normalName().endsWith(tagName)
        }

        override fun toString(): String {
            return tagName
        }
    }

    /**
     * Evaluator for element id
     */
    class Id(private val id: String) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return id == element.id()
        }

        override fun toString(): String {
            return "#$id"
        }
    }

    /**
     * Evaluator for element class
     */
    class Class(private val className: String) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.hasClass(className)
        }

        override fun toString(): String {
            return ".$className"
        }
    }

    /**
     * Evaluator for attribute name matching
     */
    class Attribute(private val key: String) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.hasAttr(key)
        }

        override fun toString(): String {
            return "[%$key]"
        }
    }

    /**
     * Evaluator for attribute name prefix matching
     */
    class AttributeStarting(keyPrefix: String) : Evaluator() {

        private val keyPrefix: String

        init {
            this.keyPrefix = keyPrefix.lowercase()
        }

        override fun matches(root: Element?, element: Element): Boolean {
            val values: List<org.jsoup.nodes.Attribute> = element.attributes().asList()
            for (attribute in values) {
                if (attribute.key.lowercase().startsWith(keyPrefix)) return true
            }
            return false
        }

        override fun toString(): String {
            return "[^$keyPrefix]"
        }
    }

    /**
     * Evaluator for attribute name/value matching
     */
    class AttributeWithValue(key: String, value: String) : AttributeKeyPair(key, value) {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.hasAttr(key) && value.equals(element.attr(key).trim(), true)
        }

        override fun toString(): String {
            return "[$key=$value]"
        }
    }

    /**
     * Evaluator for attribute name != value matching
     */
    class AttributeWithValueNot(key: String, value: String) : AttributeKeyPair(key, value) {

        override fun matches(root: Element?, element: Element): Boolean {
            return !value.equals(element.attr(key), true)
        }

        override fun toString(): String {
            return "[$key!=$value]"
        }
    }

    /**
     * Evaluator for attribute name/value matching (value prefix)
     */
    class AttributeWithValueStarting(key: String, value: String) : AttributeKeyPair(key, value, false) {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.hasAttr(key) && element.attr(key).lowercase().startsWith(value) // value is lower case already
        }

        override fun toString(): String {
            return "[$key^=$value]"
        }
    }

    /**
     * Evaluator for attribute name/value matching (value ending)
     */
    class AttributeWithValueEnding(key: String, value: String) : AttributeKeyPair(key, value, false) {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.hasAttr(key) && element.attr(key).lowercase().endsWith(value) // value is lower case
        }

        override fun toString(): String {
            return "[$key$=$value]"
        }
    }

    /**
     * Evaluator for attribute name/value matching (value containing)
     */
    class AttributeWithValueContaining(key: String, value: String) : AttributeKeyPair(key, value) {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.hasAttr(key) && element.attr(key).lowercase().contains(value) // value is lower case
        }

        override fun toString(): String {
            return "[$key*=$value]"
        }
    }

    /**
     * Evaluator for attribute name/value matching (value regex matching)
     */
    class AttributeWithValueMatching(key: String, pattern: Regex) : Evaluator() {

        val key: String
        val pattern: Regex

        init {
            this.key = normalize(key)
            this.pattern = pattern
        }
        override fun matches(root: Element?, element: Element): Boolean {
            return element.hasAttr(key) && pattern.matches(element.attr(key))
        }

        override fun toString(): String {
            return "[$key~=$pattern]"
        }
    }

    /**
     * Abstract evaluator for attribute name/value matching
     */
    abstract class AttributeKeyPair constructor(
        key: String,
        value: String,
        trimValue: Boolean = true
    ) : Evaluator() {

        var key: String
        var value: String

        init {
            var value = value
            Validate.notEmpty(key)
            Validate.notEmpty(value)
            this.key = normalize(key)
            val isStringLiteral = (value.startsWith("'") && value.endsWith("'")
                    || value.startsWith("\"") && value.endsWith("\""))
            if (isStringLiteral) {
                value = value.substring(1, value.length - 1)
            }
            this.value = if (trimValue) normalize(value) else normalize(value, isStringLiteral)
        }
    }

    /**
     * Evaluator for any / all element matching
     */
    class AllElements : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return true
        }

        override fun toString(): String {
            return "*"
        }
    }

    /**
     * Evaluator for matching by sibling index number (e &lt; idx)
     */
    class IndexLessThan(index: Int) : IndexEvaluator(index) {

        override fun matches(root: Element?, element: Element): Boolean {
            return root !== element && element.elementSiblingIndex() < index
        }

        override fun toString(): String {
            return ":lt($index)"
        }
    }

    /**
     * Evaluator for matching by sibling index number (e &gt; idx)
     */
    class IndexGreaterThan(index: Int) : IndexEvaluator(index) {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.elementSiblingIndex() > index
        }

        override fun toString(): String {
            return ":gt($index)"
        }
    }

    /**
     * Evaluator for matching by sibling index number (e = idx)
     */
    class IndexEquals(index: Int) : IndexEvaluator(index) {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.elementSiblingIndex() == index
        }

        override fun toString(): String {
            return ":eq($index)"
        }
    }

    /**
     * Evaluator for matching the last sibling (css :last-child)
     */
    class IsLastChild : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            val p = element.parent()
            return p != null && p !is Document && element.elementSiblingIndex() == p.children().size - 1
        }

        override fun toString(): String {
            return ":last-child"
        }
    }

    class IsFirstOfType : IsNthOfType(0, 1) {

        override fun toString(): String {
            return ":first-of-type"
        }
    }

    class IsLastOfType : IsNthLastOfType(0, 1) {

        override fun toString(): String {
            return ":last-of-type"
        }
    }

    abstract class CssNthEvaluator(protected val a: Int, protected val b: Int) : Evaluator() {
        constructor(b: Int) : this(0, b) {}

        override fun matches(root: Element?, element: Element): Boolean {
            val p = element.parent()
            if (p == null || p is Document) {
                return false
            }
            val pos = calculatePosition(root, element)
            return if (a == 0) {
                pos == b
            } else {
                (pos - b) * a >= 0 && (pos - b) % a == 0
            }
        }

        override fun toString(): String {
            if (a == 0) {
                return ":${getPseudoClass()}($b)"
            }
            return if (b == 0) {
                ":${getPseudoClass()}(${a}n)"
            } else {
                val symbol = if (b > 0) "+" else "-"
                ":${getPseudoClass()}(${a}n$symbol${b})"
            }
        }

        protected abstract fun getPseudoClass(): String
        protected abstract fun calculatePosition(root: Element?, element: Element): Int
    }

    /**
     * css-compatible Evaluator for :eq (css :nth-child)
     *
     * @see IndexEquals
     */
    class IsNthChild(a: Int, b: Int) : CssNthEvaluator(a, b) {

        override fun calculatePosition(root: Element?, element: Element): Int {
            return element.elementSiblingIndex() + 1
        }

        override fun getPseudoClass(): String {
            return "nth-child"
        }
    }

    /**
     * css pseudo class :nth-last-child)
     *
     * @see IndexEquals
     */
    class IsNthLastChild(a: Int, b: Int) : CssNthEvaluator(a, b) {

        override fun calculatePosition(root: Element?, element: Element): Int {
            return if (element.parent() == null) {
                0
            } else {
                element.parent()!!.children().size - element.elementSiblingIndex()
            }
        }

        override fun getPseudoClass(): String {
            return "nth-last-child"
        }
    }

    /**
     * css pseudo class nth-of-type
     *
     */
    open class IsNthOfType(a: Int, b: Int) : CssNthEvaluator(a, b) {

        override fun calculatePosition(root: Element?, element: Element): Int {
            var pos = 0
            if (element.parent() == null) {
                return 0
            }
            val family: Elements = element.parent()!!.children()
            for (el in family) {
                if (el.tag().equals(element.tag())) pos++
                if (el === element) break
            }
            return pos
        }

        override fun getPseudoClass(): String {
            return "nth-of-type"
        }
    }

    open class IsNthLastOfType(a: Int, b: Int) : CssNthEvaluator(a, b) {

        override fun calculatePosition(root: Element?, element: Element): Int {
            var pos = 0
            if (element.parent() == null) {
                return 0
            }
            val family: Elements = element.parent()!!.children()
            for (i in element.elementSiblingIndex() until family.size) {
                if (family.get(i).tag().equals(element.tag())) pos++
            }
            return pos
        }

        override fun getPseudoClass(): String {
            return "nth-last-of-type"
        }
    }

    /**
     * Evaluator for matching the first sibling (css :first-child)
     */
    class IsFirstChild : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            val p = element.parent()
            return p != null && p !is Document && element.elementSiblingIndex() == 0
        }

        override fun toString(): String {
            return ":first-child"
        }
    }

    /**
     * css3 pseudo-class :root
     * @see [:root selector](http://www.w3.org/TR/selectors/.root-pseudo)
     */
    class IsRoot : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            val r = if (root is Document) root.child(0) else root
            return element === r
        }

        override fun toString(): String {
            return ":root"
        }
    }

    class IsOnlyChild : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            val p = element.parent()
            return p != null && p !is Document && element.siblingElements().isEmpty()
        }

        override fun toString(): String {
            return ":only-child"
        }
    }

    class IsOnlyOfType : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            val p = element.parent()
            if (p == null || p is Document) return false
            var pos = 0
            val family: Elements = p.children()
            for (el in family) {
                if (el.tag().equals(element.tag())) pos++
            }
            return pos == 1
        }

        override fun toString(): String {
            return ":only-of-type"
        }
    }

    class IsEmpty : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            val family: List<Node> = element.childNodes()
            for (n in family) {
                if (!(n is Comment || n is XmlDeclaration || n is DocumentType)) return false
            }
            return true
        }

        override fun toString(): String {
            return ":empty"
        }
    }

    /**
     * Abstract evaluator for sibling index matching
     *
     * @author ant
     */
    abstract class IndexEvaluator(var index: Int) : Evaluator()

    /**
     * Evaluator for matching Element (and its descendants) text
     */
    class ContainsText(searchText: String) : Evaluator() {

        private val searchText: String
        override fun matches(root: Element?, element: Element): Boolean {
            return element.text().lowercase().contains(searchText)
        }

        override fun toString(): String {
            return ":contains($searchText)"
        }

        init {
            this.searchText = normaliseWhitespace(searchText).lowercase()
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) wholeText. Neither the input nor the element text is
     * normalized. `:containsWholeText()`
     * @since 1.15.1.
     */
    class ContainsWholeText(private val searchText: String) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.wholeText().contains(searchText)
        }

        override fun toString(): String {
            return ":containsWholeText($searchText)"
        }
    }

    /**
     * Evaluator for matching Element (but **not** its descendants) wholeText. Neither the input nor the element text is
     * normalized. `:containsWholeOwnText()`
     * @since 1.15.1.
     */
    class ContainsWholeOwnText(private val searchText: String) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return element.wholeOwnText().contains(searchText)
        }

        override fun toString(): String {
            return ":containsWholeOwnText($searchText)"
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) data
     */
    class ContainsData(searchText: String) : Evaluator() {

        private val searchText: String
        override fun matches(root: Element?, element: Element): Boolean {
            return element.data().lowercase().contains(searchText) // not whitespace normalized
        }

        override fun toString(): String {
            return ":containsData($searchText)"
        }

        init {
            this.searchText = searchText.lowercase()
        }
    }

    /**
     * Evaluator for matching Element's own text
     */
    class ContainsOwnText(searchText: String) : Evaluator() {

        private val searchText: String
        override fun matches(root: Element?, element: Element): Boolean {
            return element.ownText().lowercase().contains(searchText)
        }

        override fun toString(): String {
            return ":containsOwn($searchText)"
        }

        init {
            this.searchText = normaliseWhitespace(searchText).lowercase()
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) text with regex
     */
    class Matches(private val pattern: Regex) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return pattern.matches(element.text())
        }

        override fun toString(): String {
            return ":matches($pattern)"
        }
    }

    /**
     * Evaluator for matching Element's own text with regex
     */
    class MatchesOwn(private val pattern: Regex) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return pattern.matches(element.ownText())
        }

        override fun toString(): String {
            return ":matchesOwn($pattern)"
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) whole text with regex.
     * @since 1.15.1.
     */
    class MatchesWholeText(private val pattern: Regex) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return pattern.matches(element.wholeText())
        }

        override fun toString(): String {
            return ":matchesWholeText($pattern)"
        }
    }

    /**
     * Evaluator for matching Element's own whole text with regex.
     * @since 1.15.1.
     */
    class MatchesWholeOwnText(private val pattern: Regex) : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            return pattern.matches(element.wholeOwnText())
        }

        override fun toString(): String {
            return ":matchesWholeOwnText($pattern)"
        }
    }

    class MatchText : Evaluator() {

        override fun matches(root: Element?, element: Element): Boolean {
            if (element is PseudoTextElement) return true
            val textNodes: List<TextNode> = element.textNodes()
            for (textNode in textNodes) {
                val pel = PseudoTextElement(org.jsoup.parser.Tag.valueOf(element.tagName()), element.baseUri(), element.attributes())
                textNode.replaceWith(pel)
                pel.appendChild(textNode)
            }
            return false
        }

        override fun toString(): String {
            return ":matchText"
        }
    }
}