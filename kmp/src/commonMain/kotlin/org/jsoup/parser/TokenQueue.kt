package org.jsoup.parser

import org.jsoup.helper.Validate
import org.jsoup.internal.StringUtil
import kotlinx.*
/**
 * A character queue with parsing helpers.
 *
 * @author Jonathan Hedley
 */
class TokenQueue(data: String) {

    private var queue: String
    private var pos = 0

    /**
     * Is the queue empty?
     * @return true if no data left in queue.
     */
    fun isEmpty(): Boolean {
        return remainingLength() == 0
    }

    private fun remainingLength(): Int {
        return queue.length - pos
    }

    /**
     * Add a string to the start of the queue.
     * @param seq string to add.
     */
    fun addFirst(seq: String) {
        // not very performant, but an edge case
        queue = seq + queue.substring(pos)
        pos = 0
    }

    /**
     * Tests if the next characters on the queue match the sequence. Case insensitive.
     * @param seq String to check queue for.
     * @return true if the next characters match.
     */
    fun matches(seq: String): Boolean {
        return queue.regionMatches(pos, seq, 0, seq.length, ignoreCase = true)
    }

    /**
     * Tests if the next characters match any of the sequences. Case insensitive.
     * @param seq list of strings to case insensitively check for
     * @return true of any matched, false if none did
     */
    fun matchesAny(vararg seq: String): Boolean {
        for (s in seq) {
            if (matches(s)) return true
        }
        return false
    }

    fun matchesAny(vararg seq: Char): Boolean {
        if (isEmpty()) return false
        for (c in seq) {
            if (queue[pos] == c) return true
        }
        return false
    }

    /**
     * Tests if the queue matches the sequence (as with match), and if they do, removes the matched string from the
     * queue.
     * @param seq String to search for, and if found, remove from queue.
     * @return true if found and removed, false if not found.
     */
    fun matchChomp(seq: String): Boolean {
        return if (matches(seq)) {
            pos += seq.length
            true
        } else {
            false
        }
    }

    /**
     * Tests if queue starts with a whitespace character.
     * @return if starts with whitespace
     */
    fun matchesWhitespace(): Boolean {
        return !isEmpty() && StringUtil.isWhitespace(queue[pos].toInt())
    }

    /**
     * Test if the queue matches a word character (letter or digit).
     * @return if matches a word character
     */
    fun matchesWord(): Boolean {
        return !isEmpty() && Char.isLetterOrDigit(queue[pos])
    }

    /**
     * Drops the next character off the queue.
     */
    fun advance() {
        if (!isEmpty()) pos++
    }

    /**
     * Consume one character off queue.
     * @return first character on queue.
     */
    fun consume(): Char {
        return queue[pos++]
    }

    /**
     * Consumes the supplied sequence of the queue. If the queue does not start with the supplied sequence, will
     * throw an illegal state exception -- but you should be running match() against that condition.
     *
     *
     * Case insensitive.
     * @param seq sequence to remove from head of queue.
     */
    fun consume(seq: String) {
        check(matches(seq)) { "Queue did not match expected sequence" }
        val len = seq.length
        check(len <= remainingLength()) { "Queue not long enough to consume sequence" }
        pos += len
    }

    /**
     * Pulls a string off the queue, up to but exclusive of the match sequence, or to the queue running out.
     * @param seq String to end on (and not include in return, but leave on queue). **Case sensitive.**
     * @return The matched data consumed from queue.
     */
    fun consumeTo(seq: String?): String {
        val offset = queue.indexOf(seq!!, pos)
        return if (offset != -1) {
            val consumed = queue.substring(pos, offset)
            pos += consumed.length
            consumed
        } else {
            remainder()
        }
    }

    fun consumeToIgnoreCase(seq: String): String {
        val start = pos
        val first = seq.substring(0, 1)
        val canScan = first.toLowerCase() == first.toUpperCase() // if first is not cased, use index of
        while (!isEmpty()) {
            if (matches(seq)) break
            if (canScan) {
                val skip = queue.indexOf(first, pos) - pos
                if (skip == 0) // this char is the skip char, but not match, so force advance of pos
                    pos++ else if (skip < 0) // no chance of finding, grab to end
                    pos = queue.length else pos += skip
            } else pos++
        }
        return queue.substring(start, pos)
    }

    /**
     * Consumes to the first sequence provided, or to the end of the queue. Leaves the terminator on the queue.
     * @param seq any number of terminators to consume to. **Case insensitive.**
     * @return consumed string
     */
    // todo: method name. not good that consumeTo cares for case, and consume to any doesn't. And the only use for this
    // is is a case sensitive time...
    fun consumeToAny(vararg seq: String): String {
        val start = pos
        while (!isEmpty() && !matchesAny(*seq)) {
            pos++
        }
        return queue.substring(start, pos)
    }

    /**
     * Pulls a string off the queue (like consumeTo), and then pulls off the matched string (but does not return it).
     *
     *
     * If the queue runs out of characters before finding the seq, will return as much as it can (and queue will go
     * isEmpty() == true).
     * @param seq String to match up to, and not include in return, and to pull off queue. **Case sensitive.**
     * @return Data matched from queue.
     */
    fun chompTo(seq: String): String {
        val data = consumeTo(seq)
        matchChomp(seq)
        return data
    }

    fun chompToIgnoreCase(seq: String): String {
        val data = consumeToIgnoreCase(seq) // case insensitive scan
        matchChomp(seq)
        return data
    }

    /**
     * Pulls a balanced string off the queue. E.g. if queue is "(one (two) three) four", (,) will return "one (two) three",
     * and leave " four" on the queue. Unbalanced openers and closers can be quoted (with ' or ") or escaped (with \). Those escapes will be left
     * in the returned string, which is suitable for regexes (where we need to preserve the escape), but unsuitable for
     * contains text strings; use unescape for that.
     * @param open opener
     * @param close closer
     * @return data matched from the queue
     */
    fun chompBalanced(open: Char, close: Char): String {
        var start = -1
        var end = -1
        var depth = 0
        var last = 0.toChar()
        var inSingleQuote = false
        var inDoubleQuote = false
        var inRegexQE = false // regex \Q .. \E escapes from Pattern.quote()
        do {
            if (isEmpty()) break
            val c = consume()
            if (last != ESC) {
                if (c == '\'' && c != open && !inDoubleQuote) inSingleQuote =
                    !inSingleQuote else if (c == '"' && c != open && !inSingleQuote) inDoubleQuote = !inDoubleQuote
                if (inSingleQuote || inDoubleQuote || inRegexQE) {
                    last = c
                    continue
                }
                if (c == open) {
                    depth++
                    if (start == -1) start = pos
                } else if (c == close) depth--
            } else if (c == 'Q') {
                inRegexQE = true
            } else if (c == 'E') {
                inRegexQE = false
            }
            if (depth > 0 && last.toInt() != 0) end = pos // don't include the outer match pair in the return
            last = c
        } while (depth > 0)
        val out = if (end >= 0) queue.substring(start, end) else ""
        if (depth > 0) { // ran out of queue before seeing enough )
            Validate.fail("Did not find balanced marker at '$out'")
        }
        return out
    }

    /**
     * Pulls the next run of whitespace characters of the queue.
     * @return Whether consuming whitespace or not
     */
    fun consumeWhitespace(): Boolean {
        var seen = false
        while (matchesWhitespace()) {
            pos++
            seen = true
        }
        return seen
    }

    /**
     * Retrieves the next run of word type (letter or digit) off the queue.
     * @return String of word characters from queue, or empty string if none.
     */
    fun consumeWord(): String {
        val start = pos
        while (matchesWord()) pos++
        return queue.substring(start, pos)
    }

    /**
     * Consume a CSS element selector (tag name, but | instead of : for namespaces (or *| for wildcard namespace), to not conflict with :pseudo selects).
     *
     * @return tag name
     */
    fun consumeElementSelector(): String {
        val start = pos
        while (!isEmpty() && (matchesWord() || matchesAny("*|", "|", "_", "-"))) pos++
        return queue.substring(start, pos)
    }

    /**
     * Consume a CSS identifier (ID or class) off the queue (letter, digit, -, _)
     * http://www.w3.org/TR/CSS2/syndata.html#value-def-identifier
     * @return identifier
     */
    fun consumeCssIdentifier(): String {
        val start = pos
        while (!isEmpty() && (matchesWord() || matchesAny('-', '_'))) pos++
        return queue.substring(start, pos)
    }

    /**
     * Consume and return whatever is left on the queue.
     * @return remained of queue.
     */
    fun remainder(): String {
        val remainder = queue.substring(pos)
        pos = queue.length
        return remainder
    }

    override fun toString(): String {
        return queue.substring(pos)
    }

    companion object {

        private const val ESC = '\\' // escape char for chomp balanced.

        /**
         * Unescape a \ escaped string.
         * @param in backslash escaped string
         * @return unescaped string
         */
        fun unescape(`in`: String): String {
            val out: StringBuilder = StringUtil.borrowBuilder()
            var last = 0.toChar()
            for (c in `in`.toCharArray()) {
                if (c == ESC) {
                    if (last == ESC) out.append(c)
                } else out.append(c)
                last = c
            }
            return StringUtil.releaseBuilder(out)
        }
    }

    /**
     * Create a new TokenQueue.
     * @param data string of data to back queue.
     */
    init {
        Validate.notNull(data)
        queue = data
    }
}