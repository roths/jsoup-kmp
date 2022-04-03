package kotlinx.io

import kotlin.math.max
import kotlin.math.min

/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

/**
 * A character stream whose source is a string.
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */
class StringReader(s: String) : Reader() {

    private var str: String?
    private val length: Int
    private var next = 0
    private var mark = 0

    /**
     * Creates a new string reader.
     *
     * @param s  String providing the character stream.
     */
    init {
        str = s
        length = s.length
    }

    /** Check to make sure that the stream has not been closed  */
    @Throws(IOException::class)
    private fun ensureOpen() {
        if (str == null) throw IOException("Stream closed")
    }

    /**
     * Reads a single character.
     *
     * @return     The character read, or -1 if the end of the stream has been
     * reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Throws(IOException::class)
    override fun read(): Int {
//        synchronized(lock) {
            ensureOpen()
            return if (next >= length) -1 else str!![next++].toInt()
//        }
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @param      cbuf  Destination buffer
     * @param      off   Offset at which to start writing characters
     * @param      len   Maximum number of characters to read
     *
     * @return     The number of characters read, or -1 if the end of the
     * stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Throws(IOException::class)
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
//        synchronized(lock) {
            ensureOpen()
            if (off < 0 || off > cbuf.size || len < 0 ||
                off + len > cbuf.size || off + len < 0
            ) {
                throw IndexOutOfBoundsException()
            } else if (len == 0) {
                return 0
            }
            if (next >= length) return -1
            val n = min(length - next, len)
            str!!.toCharArray(next, next + n).copyInto(cbuf, off)
            next += n
            return n
//        }
    }

    /**
     * Skips the specified number of characters in the stream. Returns
     * the number of characters that were skipped.
     *
     *
     * The `ns` parameter may be negative, even though the
     * `skip` method of the [Reader] superclass throws
     * an exception in this case. Negative values of `ns` cause the
     * stream to skip backwards. Negative return values indicate a skip
     * backwards. It is not possible to skip backwards past the beginning of
     * the string.
     *
     *
     * If the entire string has been read or skipped, then this method has
     * no effect and always returns 0.
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Throws(IOException::class)
    override fun skip(ns: Long): Long {
//        synchronized(lock) {
            ensureOpen()
            if (next >= length) return 0
            // Bound skip by beginning and end of the source
            var n = min((length - next).toLong(), ns)
            n = max(-next.toLong(), n)
            next += n.toInt()
            return n
//        }
    }

    /**
     * Tells whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input
     *
     * @exception  IOException  If the stream is closed
     */
    @Throws(IOException::class)
    override fun ready(): Boolean {
//        synchronized(lock) {
            ensureOpen()
            return true
//        }
    }

    /**
     * Tells whether this stream supports the mark() operation, which it does.
     */
    override fun markSupported(): Boolean {
        return true
    }

    /**
     * Marks the present position in the stream.  Subsequent calls to reset()
     * will reposition the stream to this point.
     *
     * @param  readAheadLimit  Limit on the number of characters that may be
     * read while still preserving the mark.  Because
     * the stream's input comes from a string, there
     * is no actual limit, so this argument must not
     * be negative, but is otherwise ignored.
     *
     * @exception  IllegalArgumentException  If `readAheadLimit < 0`
     * @exception  IOException  If an I/O error occurs
     */
    @Throws(IOException::class)
    override fun mark(readAheadLimit: Int) {
        require(readAheadLimit >= 0) { "Read-ahead limit < 0" }
//        synchronized(lock) {
            ensureOpen()
            mark = next
//        }
    }

    /**
     * Resets the stream to the most recent mark, or to the beginning of the
     * string if it has never been marked.
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Throws(IOException::class)
    override fun reset() {
//        synchronized(lock) {
            ensureOpen()
            next = mark
//        }
    }

    /**
     * Closes the stream and releases any system resources associated with
     * it. Once the stream has been closed, further read(),
     * ready(), mark(), or reset() invocations will throw an IOException.
     * Closing a previously closed stream has no effect.
     */
    override fun close() {
        str = null
    }
}
