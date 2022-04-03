package org.jsoup.select

import org.jsoup.helper.Validate
import org.jsoup.internal.Normalizer.normalize
import org.jsoup.internal.StringUtil
import org.jsoup.parser.TokenQueue
import org.jsoup.select.CombiningEvaluator.And
import org.jsoup.select.CombiningEvaluator.Or
import org.jsoup.select.Evaluator.*
import org.jsoup.select.Selector.SelectorParseException
import org.jsoup.select.StructuralEvaluator.*
import kotlin.text.RegexOption.IGNORE_CASE

/**
 * Parses a CSS selector into an Evaluator tree.
 */
class QueryParser private constructor(query: String) {

    private val tq: TokenQueue
    private val query: String
    private val evals: MutableList<Evaluator> = ArrayList()

    /**
     * Create a new QueryParser.
     * @param query CSS query
     */
    init {
        var query = query
        Validate.notEmpty(query)
        query = query.trim { it <= ' ' }
        this.query = query
        tq = TokenQueue(query)
    }

    /**
     * Parse the query
     * @return Evaluator
     */
    fun parse(): Evaluator {
        tq.consumeWhitespace()
        if (tq.matchesAny(*combinators)) { // if starts with a combinator, use root as elements
            evals.add(Root())
            combinator(tq.consume())
        } else {
            findElements()
        }
        while (!tq.isEmpty()) {
            // hierarchy and extras
            val seenWhite: Boolean = tq.consumeWhitespace()
            if (tq.matchesAny(*combinators)) {
                combinator(tq.consume())
            } else if (seenWhite) {
                combinator(' ')
            } else { // E.class, E#id, E[attr] etc. AND
                findElements() // take next el, #. etc off queue
            }
        }
        return if (evals.size == 1) evals[0] else And(evals)
    }

    private fun combinator(combinator: Char) {
        tq.consumeWhitespace()
        val subQuery = consumeSubQuery() // support multi > childs
        var rootEval: Evaluator? // the new topmost evaluator
        var currentEval: Evaluator? // the evaluator the new eval will be combined to. could be root, or rightmost or.
        val newEval = parse(subQuery) // the evaluator to add into target evaluator
        var replaceRightMost = false
        if (evals.size == 1) {
            currentEval = evals[0]
            rootEval = currentEval
            // make sure OR (,) has precedence:
            if (rootEval is CombiningEvaluator.Or && combinator != ',') {
                currentEval = (currentEval as CombiningEvaluator.Or).rightMostEvaluator()
                replaceRightMost = true
            }
        } else {
            currentEval = And(evals)
            rootEval = currentEval
        }
        evals.clear()
        when (combinator) {
            '>' -> currentEval = And(ImmediateParent(currentEval!!), newEval)
            ' ' -> currentEval = And(Parent(currentEval!!), newEval)
            '+' -> currentEval = And(ImmediatePreviousSibling(currentEval!!), newEval)
            '~' -> currentEval = And(PreviousSibling(currentEval!!), newEval)
            ',' -> {
                val or: CombiningEvaluator.Or
                if (currentEval is CombiningEvaluator.Or) {
                    or = currentEval as CombiningEvaluator.Or
                } else {
                    or = Or()
                    or.add(currentEval!!)
                }
                or.add(newEval)
                currentEval = or
            }
            else -> throw SelectorParseException("Unknown combinator '%s'", combinator)
        }
        if (replaceRightMost) {
            (rootEval as CombiningEvaluator.Or).replaceRightMostEvaluator(currentEval)
        } else {
            rootEval = currentEval
        }
        evals.add(rootEval)
    }

    private fun consumeSubQuery(): String {
        val sq: StringBuilder = StringUtil.borrowBuilder()
        while (!tq.isEmpty()) {
            if (tq.matches("(")) sq.append("(").append(tq.chompBalanced('(', ')'))
                .append(")") else if (tq.matches("[")) sq.append("[").append(tq.chompBalanced('[', ']'))
                .append("]") else if (tq.matchesAny(*combinators)
            ) if (sq.length > 0) break else tq.consume() else sq.append(tq.consume())
        }
        return StringUtil.releaseBuilder(sq)
    }

    private fun findElements() {
        if (tq.matchChomp("#")) byId() else if (tq.matchChomp(".")) byClass() else if (tq.matchesWord() || tq.matches(
                "*|"
            )
        ) byTag() else if (tq.matches("[")) byAttribute() else if (tq.matchChomp("*")) allElements() else if (tq.matchChomp(
                ":lt("
            )
        ) indexLessThan() else if (tq.matchChomp(":gt(")) indexGreaterThan() else if (tq.matchChomp(":eq(")) indexEquals() else if (tq.matches(
                ":has("
            )
        ) has() else if (tq.matches(":contains(")) contains(false) else if (tq.matches(":containsOwn(")) contains(true) else if (tq.matches(
                ":containsWholeText("
            )
        ) containsWholeText(false) else if (tq.matches(":containsWholeOwnText(")) containsWholeText(true) else if (tq.matches(
                ":containsData("
            )
        ) containsData() else if (tq.matches(":matches(")) matches(false) else if (tq.matches(":matchesOwn(")) matches(
            true
        ) else if (tq.matches(":matchesWholeText(")) matchesWholeText(false) else if (tq.matches(":matchesWholeOwnText(")) matchesWholeText(
            true
        ) else if (tq.matches(":not(")) not() else if (tq.matchChomp(":nth-child(")) cssNthChild(
            false,
            false
        ) else if (tq.matchChomp(":nth-last-child(")) cssNthChild(
            true,
            false
        ) else if (tq.matchChomp(":nth-of-type(")) cssNthChild(
            false,
            true
        ) else if (tq.matchChomp(":nth-last-of-type(")) cssNthChild(
            true,
            true
        ) else if (tq.matchChomp(":first-child")) evals.add(IsFirstChild()) else if (tq.matchChomp(":last-child")) evals.add(
            IsLastChild()
        ) else if (tq.matchChomp(":first-of-type")) evals.add(IsFirstOfType()) else if (tq.matchChomp(":last-of-type")) evals.add(
            IsLastOfType()
        ) else if (tq.matchChomp(":only-child")) evals.add(IsOnlyChild()) else if (tq.matchChomp(":only-of-type")) evals.add(
            IsOnlyOfType()
        ) else if (tq.matchChomp(":empty")) evals.add(IsEmpty()) else if (tq.matchChomp(":root")) evals.add(IsRoot()) else if (tq.matchChomp(
                ":matchText"
            )
        ) evals.add(MatchText()) else throw SelectorParseException(
            "Could not parse query '%s': unexpected token at '%s'",
            query,
            tq.remainder()
        )
    }

    private fun byId() {
        val id: String = tq.consumeCssIdentifier()
        Validate.notEmpty(id)
        evals.add(Id(id))
    }

    private fun byClass() {
        val className: String = tq.consumeCssIdentifier()
        Validate.notEmpty(className)
        evals.add(Class(className.trim { it <= ' ' }))
    }

    private fun byTag() {
        // todo - these aren't dealing perfectly with case sensitivity. For case sensitive parsers, we should also make
        // the tag in the selector case-sensitive (and also attribute names). But for now, normalize (lower-case) for
        // consistency - both the selector and the element tag
        var tagName: String = normalize(tq.consumeElementSelector())
        Validate.notEmpty(tagName)

        // namespaces: wildcard match equals(tagName) or ending in ":"+tagName
        if (tagName.startsWith("*|")) {
            val plainTag = tagName.substring(2) // strip *|
            evals.add(
                Or(
                    Tag(plainTag),
                    TagEndsWith(tagName.replace("*|", ":"))
                )
            )
        } else {
            // namespaces: if element name is "abc:def", selector must be "abc|def", so flip:
            if (tagName.contains("|")) tagName = tagName.replace("|", ":")
            evals.add(Tag(tagName))
        }
    }

    private fun byAttribute() {
        val cq = TokenQueue(tq.chompBalanced('[', ']')) // content queue
        val key: String = cq.consumeToAny(*AttributeEvals) // eq, not, start, end, contain, match, (no val)
        Validate.notEmpty(key)
        cq.consumeWhitespace()
        if (cq.isEmpty()) {
            if (key.startsWith("^")) evals.add(AttributeStarting(key.substring(1))) else evals.add(Attribute(key))
        } else {
            if (cq.matchChomp("=")) evals.add(
                AttributeWithValue(
                    key,
                    cq.remainder()
                )
            ) else if (cq.matchChomp("!=")) evals.add(
                AttributeWithValueNot(
                    key,
                    cq.remainder()
                )
            ) else if (cq.matchChomp("^=")) evals.add(
                AttributeWithValueStarting(
                    key,
                    cq.remainder()
                )
            ) else if (cq.matchChomp("$=")) evals.add(
                AttributeWithValueEnding(
                    key,
                    cq.remainder()
                )
            ) else if (cq.matchChomp("*=")) evals.add(
                AttributeWithValueContaining(
                    key,
                    cq.remainder()
                )
            ) else if (cq.matchChomp("~=")) evals.add(
                AttributeWithValueMatching(
                    key,
                    cq.remainder().toRegex()
                )
            ) else throw SelectorParseException(
                "Could not parse attribute query '%s': unexpected token at '%s'",
                query,
                cq.remainder()
            )
        }
    }

    private fun allElements() {
        evals.add(AllElements())
    }

    // pseudo selectors :lt, :gt, :eq
    private fun indexLessThan() {
        evals.add(IndexLessThan(consumeIndex()))
    }

    private fun indexGreaterThan() {
        evals.add(IndexGreaterThan(consumeIndex()))
    }

    private fun indexEquals() {
        evals.add(IndexEquals(consumeIndex()))
    }

    private fun cssNthChild(backwards: Boolean, ofType: Boolean) {
        val argS: String = normalize(tq.chompTo(")"))

        val mAB = NTH_AB.matchEntire(argS)
        val mB = NTH_B.matchEntire(argS)
        val a: Int
        val b: Int
        if ("odd" == argS) {
            a = 2
            b = 1
        } else if ("even" == argS) {
            a = 2
            b = 0
        } else if (mAB != null) {
            a = if (mAB.groupValues[3].isNotEmpty()) mAB.groupValues[1].replaceFirst("^\\+".toRegex(), "").toInt() else 1
            b = if (mAB.groupValues[4].isNotEmpty()) mAB.groupValues[4].replaceFirst("^\\+".toRegex(), "").toInt() else 0
        } else if (mB != null) {
            a = 0
            b = mB.groupValues[0].replaceFirst("^\\+".toRegex(), "").toInt()
        } else {
            throw SelectorParseException("Could not parse nth-index '%s': unexpected format", argS)
        }
        if (ofType) if (backwards) evals.add(IsNthLastOfType(a, b)) else evals.add(IsNthOfType(a, b)) else {
            if (backwards) evals.add(IsNthLastChild(a, b)) else evals.add(IsNthChild(a, b))
        }
    }

    private fun consumeIndex(): Int {
        val indexS: String = tq.chompTo(")").trim()
        Validate.isTrue(StringUtil.isNumeric(indexS), "Index must be numeric")
        return indexS.toInt()
    }

    // pseudo selector :has(el)
    private fun has() {
        tq.consume(":has")
        val subQuery: String = tq.chompBalanced('(', ')')
        Validate.notEmpty(subQuery, ":has(selector) subselect must not be empty")
        evals.add(Has(parse(subQuery)))
    }

    // pseudo selector :contains(text), containsOwn(text)
    private fun contains(own: Boolean) {
        val query = if (own) ":containsOwn" else ":contains"
        tq.consume(query)
        val searchText: String = TokenQueue.unescape(tq.chompBalanced('(', ')'))
        Validate.notEmpty(searchText, "$query(text) query must not be empty")
        evals.add(if (own) ContainsOwnText(searchText) else ContainsText(searchText))
    }

    private fun containsWholeText(own: Boolean) {
        val query = if (own) ":containsWholeOwnText" else ":containsWholeText"
        tq.consume(query)
        val searchText: String = TokenQueue.unescape(tq.chompBalanced('(', ')'))
        Validate.notEmpty(searchText, "$query(text) query must not be empty")
        evals.add(if (own) ContainsWholeOwnText(searchText) else ContainsWholeText(searchText))
    }

    // pseudo selector :containsData(data)
    private fun containsData() {
        tq.consume(":containsData")
        val searchText: String = TokenQueue.unescape(tq.chompBalanced('(', ')'))
        Validate.notEmpty(searchText, ":containsData(text) query must not be empty")
        evals.add(ContainsData(searchText))
    }

    // :matches(regex), matchesOwn(regex)
    private fun matches(own: Boolean) {
        val query = if (own) ":matchesOwn" else ":matches"
        tq.consume(query)
        val regex: String = tq.chompBalanced('(', ')') // don't unescape, as regex bits will be escaped
        Validate.notEmpty(regex, "$query(regex) query must not be empty")
        evals.add(if (own) MatchesOwn(regex.toRegex()) else Matches(regex.toRegex()))
    }

    // :matches(regex), matchesOwn(regex)
    private fun matchesWholeText(own: Boolean) {
        val query = if (own) ":matchesWholeOwnText" else ":matchesWholeText"
        tq.consume(query)
        val regex: String = tq.chompBalanced('(', ')') // don't unescape, as regex bits will be escaped
        evals.add(if (own) MatchesWholeOwnText(regex.toRegex()) else MatchesWholeText(regex.toRegex()))
    }

    // :not(selector)
    private operator fun not() {
        tq.consume(":not")
        val subQuery: String = tq.chompBalanced('(', ')')
        Validate.notEmpty(subQuery, ":not(selector) subselect must not be empty")
        evals.add(Not(parse(subQuery)))
    }

    override fun toString(): String {
        return query
    }

    companion object {

        private val combinators = arrayOf(",", ">", "+", "~", " ")
        private val AttributeEvals = arrayOf("=", "!=", "^=", "$=", "*=", "~=")

        /**
         * Parse a CSS query into an Evaluator.
         * @param query CSS query
         * @return Evaluator
         * @see Selector selector query syntax
         */
        fun parse(query: String): Evaluator {
            return try {
                val p = QueryParser(query)
                p.parse()
            } catch (e: IllegalArgumentException) {
                throw SelectorParseException(e.message)
            }
        }

        //pseudo selectors :first-child, :last-child, :nth-child, ...
        private val NTH_AB = """(([+-])?(\d+)?)n(\s*([+-])?\s*\d+)?""".toRegex(IGNORE_CASE)
        private val NTH_B = """([+-])?(\d+)""".toRegex()
    }
}