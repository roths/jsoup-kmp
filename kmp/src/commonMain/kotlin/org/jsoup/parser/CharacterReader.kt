package org.jsoup.parser

import io.ktor.utils.io.ByteReadChannel
import kotlinx.*
import kotlinx.io.Reader
import kotlinx.io.StringReader
import kotlinx.io.IOException
import org.jsoup.UncheckedIOException
import org.jsoup.helper.Validate
import kotlin.jvm.JvmOverloads
import kotlin.math.abs
import kotlin.math.min

/**
 * CharacterReader consumes tokens off a string. Used internally by jsoup. API subject to changes.
 */
class CharacterReader constructor(input: Reader, sz: Int = maxBufferLen) {

    private var charBuf: CharArray?
    private var reader: Reader?
    private var bufLength = 0
    private var bufSplitPoint = 0
    private var bufPos = 0
    private var readerPos = 0
    private var bufMark = -1
    private var stringCache: Array<String?>? =
        arrayOfNulls(stringCacheSize) // holds reused strings in this doc, to lessen garbage

    private var newlinePositions: ArrayList<Int>? =
        null // optionally track the pos() position of newlines - scans during bufferUp()
    private var lineNumberOffset = 1 // line numbers start at 1; += newlinePosition[indexof(pos)]

    constructor(input: String) : this(StringReader(input), input.length) {}

    init {
        Validate.isTrue(input.markSupported())
        reader = input
        charBuf = CharArray(min(sz, maxBufferLen))
        bufferUp()
    }

    fun close() {
        if (reader == null) return
        try {
            reader!!.close()
        } catch (ignored: IOException) {
        } finally {
            reader = null
            charBuf = null
            stringCache = null
        }
    }

    private var readFully // if the underlying stream has been completely read, no value in further buffering
            = false

    private fun bufferUp() {
        if (readFully || bufPos < bufSplitPoint) return
        val pos: Int
        val offset: Int
        if (bufMark != -1) {
            pos = bufMark
            offset = bufPos - bufMark
        } else {
            pos = bufPos
            offset = 0
        }
        try {
            val skipped = reader!!.skip(pos.toLong())
            reader!!.mark(maxBufferLen)
            var read = 0
            while (read <= minReadAheadLen) {
                val thisRead = reader!!.read(charBuf!!, read, charBuf!!.size - read)
                if (thisRead == -1) readFully = true
                if (thisRead <= 0) break
                read += thisRead
            }
            reader!!.reset()
            if (read > 0) {
                Validate.isTrue(skipped == pos.toLong()) // Previously asserted that there is room in buf to skip, so this will be a WTF
                bufLength = read
                readerPos += pos
                bufPos = offset
                if (bufMark != -1) bufMark = 0
                bufSplitPoint = min(bufLength, readAheadLimit)
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
        scanBufferForNewlines() // if enabled, we index newline positions for line number tracking
        lastIcSeq = null // cache for last containsIgnoreCase(seq)
    }

    /**
     * Gets the current cursor position in the content.
     * @return current position
     */
    fun pos(): Int {
        return readerPos + bufPos
    }

    /**
     * Enables or disables line number tracking. By default, will be **off**.Tracking line numbers improves the
     * legibility of parser error messages, for example. Tracking should be enabled before any content is read to be of
     * use.
     *
     * @param track set tracking on|off
     * @since 1.14.3
     */
    fun trackNewlines(track: Boolean) {
        if (track && newlinePositions == null) {
            newlinePositions = ArrayList(maxBufferLen / 80) // rough guess of likely count
            scanBufferForNewlines() // first pass when enabled; subsequently called during bufferUp
        } else if (!track) newlinePositions = null
    }

    /**
     * Check if the tracking of newlines is enabled.
     * @return the current newline tracking state
     * @since 1.14.3
     */
    fun isTrackNewlines(): Boolean {
        return newlinePositions != null
    }

    /**
     * Get the current line number (that the reader has consumed to). Starts at line #1.
     * @return the current line number, or 1 if line tracking is not enabled.
     * @since 1.14.3
     * @see .trackNewlines
     */
    fun lineNumber(): Int {
        if (!isTrackNewlines()) return 1
        val i = lineNumIndex()
        if (i == -1) return lineNumberOffset // first line
        return if (i < 0) abs(i) + lineNumberOffset - 1 else i + lineNumberOffset + 1
    }

    /**
     * Get the current column number (that the reader has consumed to). Starts at column #1.
     * @return the current column number
     * @since 1.14.3
     * @see .trackNewlines
     */
    fun columnNumber(): Int {
        if (!isTrackNewlines()) return pos() + 1
        var i = lineNumIndex()
        if (i == -1) return pos() + 1
        if (i < 0) i = abs(i) - 2
        return pos() - newlinePositions!![i] + 1
    }

    /**
     * Get a formatted string representing the current line and cursor positions. E.g. `5:10` indicating line
     * number 5 and column number 10.
     * @return line:col position
     * @since 1.14.3
     * @see .trackNewlines
     */
    fun cursorPos(): String {
        return lineNumber().toString() + ":" + columnNumber()
    }

    private fun lineNumIndex(): Int {
        return if (!isTrackNewlines()) 0 else newlinePositions!!.binarySearch(pos())
    }

    /**
     * Scans the buffer for newline position, and tracks their location in newlinePositions.
     */
    private fun scanBufferForNewlines() {
        if (!isTrackNewlines()) return
        lineNumberOffset += newlinePositions!!.size
        val lastPos = if (newlinePositions!!.size > 0) newlinePositions!![newlinePositions!!.size - 1] else -1
        newlinePositions!!.clear()
        if (lastPos != -1) {
            newlinePositions!!.add(lastPos) // roll the last pos to first, for cursor num after buffer
            lineNumberOffset-- // as this takes a position
        }
        for (i in bufPos until bufLength) {
            if (charBuf!![i] == '\n') newlinePositions!!.add(1 + readerPos + i)
        }
    }

    /**
     * Tests if all the content has been read.
     * @return true if nothing left to read.
     */
    fun isEmpty(): Boolean {
        bufferUp()
        return bufPos >= bufLength
    }

    private fun isEmptyNoBufferUp(): Boolean {
        return bufPos >= bufLength
    }

    /**
     * Get the char at the current position.
     * @return char
     */
    fun current(): Char {
        bufferUp()
        return if (isEmptyNoBufferUp()) EOF else charBuf!![bufPos]
    }

    fun consume(): Char {
        bufferUp()
        val `val` = if (isEmptyNoBufferUp()) EOF else charBuf!![bufPos]
        bufPos++
        return `val`
    }

    /**
     * Unconsume one character (bufPos--). MUST only be called directly after a consume(), and no chance of a bufferUp.
     */
    fun unconsume() {
        if (bufPos < 1) throw UncheckedIOException(IOException("WTF: No buffer left to unconsume.")) // a bug if this fires, need to trace it.
        bufPos--
    }

    /**
     * Moves the current position by one.
     */
    fun advance() {
        bufPos++
    }

    fun mark() {
        // make sure there is enough look ahead capacity
        if (bufLength - bufPos < minReadAheadLen) bufSplitPoint = 0
        bufferUp()
        bufMark = bufPos
    }

    fun unmark() {
        bufMark = -1
    }

    fun rewindToMark() {
        if (bufMark == -1) throw UncheckedIOException(IOException("Mark invalid"))
        bufPos = bufMark
        unmark()
    }

    /**
     * Returns the number of characters between the current position and the next instance of the input char
     * @param c scan target
     * @return offset between current position and next instance of target. -1 if not found.
     */
    fun nextIndexOf(c: Char): Int {
        // doesn't handle scanning for surrogates
        bufferUp()
        for (i in bufPos until bufLength) {
            if (c == charBuf!![i]) return i - bufPos
        }
        return -1
    }

    /**
     * Returns the number of characters between the current position and the next instance of the input sequence
     *
     * @param seq scan target
     * @return offset between current position and next instance of target. -1 if not found.
     */
    fun nextIndexOf(seq: CharSequence): Int {
        bufferUp()
        // doesn't handle scanning for surrogates
        val startChar = seq[0]
        var offset = bufPos
        while (offset < bufLength) {

            // scan to first instance of startchar:
            if (startChar != charBuf!![offset]) while (++offset < bufLength && startChar != charBuf!![offset]) { /* empty */
            }
            var i = offset + 1
            val last = i + seq.length - 1
            if (offset < bufLength && last <= bufLength) {
                var j = 1
                while (i < last && seq[j] == charBuf!![i]) {
                    i++
                    j++
                }
                if (i == last) // found full sequence
                    return offset - bufPos
            }
            offset++
        }
        return -1
    }

    /**
     * Reads characters up to the specific char.
     * @param c the delimiter
     * @return the chars read
     */
    fun consumeTo(c: Char): String {
        val offset = nextIndexOf(c)
        return if (offset != -1) {
            val consumed = cacheString(charBuf, stringCache, bufPos, offset)
            bufPos += offset
            consumed
        } else {
            consumeToEnd()
        }
    }

    fun consumeTo(seq: String): String {
        val offset = nextIndexOf(seq)
        return if (offset != -1) {
            val consumed = cacheString(charBuf, stringCache, bufPos, offset)
            bufPos += offset
            consumed
        } else if (bufLength - bufPos < seq.length) {
            // nextIndexOf() did a bufferUp(), so if the buffer is shorter than the search string, we must be at EOF
            consumeToEnd()
        } else {
            // the string we're looking for may be straddling a buffer boundary, so keep (length - 1) characters
            // unread in case they contain the beginning of the search string
            val endPos = bufLength - seq.length + 1
            val consumed =
                cacheString(charBuf, stringCache, bufPos, endPos - bufPos)
            bufPos = endPos
            consumed
        }
    }

    /**
     * Read characters until the first of any delimiters is found.
     * @param chars delimiters to scan for
     * @return characters read up to the matched delimiter.
     */
    fun consumeToAny(vararg chars: Char): String {
        bufferUp()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val `val` = charBuf
        val charLen = chars.size
        var i: Int
        OUTER@ while (pos < remaining) {
            i = 0
            while (i < charLen) {
                if (`val`!![pos] == chars[i]) break@OUTER
                i++
            }
            pos++
        }
        bufPos = pos
        return if (pos > start) cacheString(charBuf, stringCache, start, pos - start) else ""
    }

    fun consumeToAnySorted(vararg chars: Char): String {
        bufferUp()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val `val` = charBuf
        while (pos < remaining) {
            if (chars.toList().binarySearch(`val`!![pos]) >= 0) break
            pos++
        }
        bufPos = pos
        return if (bufPos > start) cacheString(charBuf, stringCache, start, pos - start) else ""
    }

    fun consumeData(): String {
        // &, <, null
        //bufferUp(); // no need to bufferUp, just called consume()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val `val` = charBuf
        OUTER@ while (pos < remaining) {
            when (`val`!![pos]) {
                '&', '<', TokeniserState.nullChar -> break@OUTER
                else -> pos++
            }
        }
        bufPos = pos
        return if (pos > start) cacheString(charBuf, stringCache, start, pos - start) else ""
    }

    fun consumeAttributeQuoted(single: Boolean): String {
        // null, " or ', &
        //bufferUp(); // no need to bufferUp, just called consume()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val `val` = charBuf
        OUTER@ while (pos < remaining) {
            when (`val`!![pos]) {
                '&', TokeniserState.nullChar -> break@OUTER
                '\'' -> {
                    if (single) break@OUTER
                    if (!single) break@OUTER
                    pos++
                }
                '"' -> {
                    if (!single) break@OUTER
                    pos++
                }
                else -> pos++
            }
        }
        bufPos = pos
        return if (pos > start) cacheString(charBuf, stringCache, start, pos - start) else ""
    }

    fun consumeRawData(): String {
        // <, null
        //bufferUp(); // no need to bufferUp, just called consume()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val `val` = charBuf
        OUTER@ while (pos < remaining) {
            when (`val`!![pos]) {
                '<', TokeniserState.nullChar -> break@OUTER
                else -> pos++
            }
        }
        bufPos = pos
        return if (pos > start) cacheString(charBuf, stringCache, start, pos - start) else ""
    }

    fun consumeTagName(): String {
        // '\t', '\n', '\r', '\f', ' ', '/', '>'
        // NOTE: out of spec, added '<' to fix common author bugs; does not stop and append on nullChar but eats
        bufferUp()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val `val` = charBuf
        OUTER@ while (pos < remaining) {
            when (`val`!![pos]) {
                '\t', '\n', '\r', '\u000C', ' ', '/', '>', '<' -> break@OUTER
            }
            pos++
        }
        bufPos = pos
        return if (pos > start) cacheString(charBuf, stringCache, start, pos - start) else ""
    }

    fun consumeToEnd(): String {
        bufferUp()
        val data = cacheString(charBuf, stringCache, bufPos, bufLength - bufPos)
        bufPos = bufLength
        return data
    }

    fun consumeLetterSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf!![bufPos]
            if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || Char.isLetter(c)) bufPos++ else break
        }
        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun consumeLetterThenDigitSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf!![bufPos]
            if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || Char.isLetter(c)) bufPos++ else break
        }
        while (!isEmptyNoBufferUp()) {
            val c = charBuf!![bufPos]
            if (c >= '0' && c <= '9') bufPos++ else break
        }
        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun consumeHexSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf!![bufPos]
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f') bufPos++ else break
        }
        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun consumeDigitSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf!![bufPos]
            if (c >= '0' && c <= '9') bufPos++ else break
        }
        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun matches(c: Char): Boolean {
        return !isEmpty() && charBuf!![bufPos] == c
    }

    fun matches(seq: String): Boolean {
        bufferUp()
        val scanLength = seq.length
        if (scanLength > bufLength - bufPos) return false
        for (offset in 0 until scanLength) if (seq[offset] != charBuf!![bufPos + offset]) return false
        return true
    }

    fun matchesIgnoreCase(seq: String): Boolean {
        bufferUp()
        val scanLength = seq.length
        if (scanLength > bufLength - bufPos) return false
        for (offset in 0 until scanLength) {
            val upScan = Char.toUpperCase(seq[offset])
            val upTarget = Char.toUpperCase(charBuf!![bufPos + offset])
            if (upScan != upTarget) return false
        }
        return true
    }

    fun matchesAny(vararg seq: Char): Boolean {
        if (isEmpty()) return false
        bufferUp()
        val c = charBuf!![bufPos]
        for (seek in seq) {
            if (seek == c) return true
        }
        return false
    }

    fun matchesAnySorted(seq: CharArray): Boolean {
        bufferUp()

        return !isEmpty() && seq.toList().binarySearch(charBuf!![bufPos]) >= 0
    }

    fun matchesLetter(): Boolean {
        if (isEmpty()) return false
        val c = charBuf!![bufPos]
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || Char.isLetter(c)
    }

    /**
     * Checks if the current pos matches an ascii alpha (A-Z a-z) per https://infra.spec.whatwg.org/#ascii-alpha
     * @return if it matches or not
     */
    fun matchesAsciiAlpha(): Boolean {
        if (isEmpty()) return false
        val c = charBuf!![bufPos]
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z'
    }

    fun matchesDigit(): Boolean {
        if (isEmpty()) return false
        val c = charBuf!![bufPos]
        return c >= '0' && c <= '9'
    }

    fun matchConsume(seq: String): Boolean {
        bufferUp()
        return if (matches(seq)) {
            bufPos += seq.length
            true
        } else {
            false
        }
    }

    fun matchConsumeIgnoreCase(seq: String): Boolean {
        return if (matchesIgnoreCase(seq)) {
            bufPos += seq.length
            true
        } else {
            false
        }
    }

    // we maintain a cache of the previously scanned sequence, and return that if applicable on repeated scans.
    // that improves the situation where there is a sequence of <p<p<p<p<p<p<p...</title> and we're bashing on the <p
    // looking for the </title>. Resets in bufferUp()
    private var lastIcSeq // scan cache
            : String? = null
    private var lastIcIndex // nearest found indexOf
            = 0

    /** Used to check presence of ,  when we're in RCData and see a <xxx. Only finds consistent case.></xxx.>  */
    fun containsIgnoreCase(seq: String): Boolean {
        if (seq == lastIcSeq) {
            if (lastIcIndex == -1) return false
            if (lastIcIndex >= bufPos) return true
        }
        lastIcSeq = seq
        val loScan = seq.lowercase()
        val lo = nextIndexOf(loScan)
        if (lo > -1) {
            lastIcIndex = bufPos + lo
            return true
        }
        val hiScan = seq.lowercase()
        val hi = nextIndexOf(hiScan)
        val found = hi > -1
        lastIcIndex =
            if (found) bufPos + hi else -1 // we don't care about finding the nearest, just that buf contains
        return found
    }

    override fun toString(): String {
        return if (bufLength - bufPos < 0) {
            ""
        } else {
            charBuf!!.concatToString(bufPos, bufPos + (bufLength - bufPos))
        }
    }

    // just used for testing
    fun rangeEquals(start: Int, count: Int, cached: String): Boolean {
        return rangeEquals(charBuf, start, count, cached)
    }

    companion object {

        const val EOF = (-1).toChar()
        private const val maxStringCacheLen = 12
        const val maxBufferLen = 1024 * 32 // visible for testing
        const val readAheadLimit = (maxBufferLen * 0.75).toInt() // visible for testing
        private const val minReadAheadLen =
            1024 // the minimum mark length supported. No HTML entities can be larger than this.
        private const val stringCacheSize = 512

        /**
         * Caches short strings, as a flyweight pattern, to reduce GC load. Just for this doc, to prevent leaks.
         *
         *
         * Simplistic, and on hash collisions just falls back to creating a new string, vs a full HashMap with Entry list.
         * That saves both having to create objects as hash keys, and running through the entry list, at the expense of
         * some more duplicates.
         */
        private fun cacheString(charBuf: CharArray?, stringCache: Array<String?>?, start: Int, count: Int): String {
            // limit (no cache):
            if (count > maxStringCacheLen) return charBuf!!.concatToString(start, start + count)
            if (count < 1) return ""

            // calculate hash:
            var hash = 0
            for (i in 0 until count) {
                hash = 31 * hash + charBuf!![start + i].code
            }

            // get from cache
            val index = hash and stringCacheSize - 1
            var cached = stringCache!![index]
            if (cached != null && rangeEquals(charBuf, start, count, cached)) // positive hit
                return cached else {
                cached = charBuf!!.concatToString(start, start + count)
                stringCache[index] =
                    cached // add or replace, assuming most recently used are most likely to recur next
            }
            return cached
        }

        /**
         * Check if the value of the provided range equals the string.
         */
        fun rangeEquals(charBuf: CharArray?, start: Int, count: Int, cached: String): Boolean {
            var count = count
            if (count == cached.length) {
                var i = start
                var j = 0
                while (count-- != 0) {
                    if (charBuf!![i++] != cached[j++]) return false
                }
                return true
            }
            return false
        }
    }
}