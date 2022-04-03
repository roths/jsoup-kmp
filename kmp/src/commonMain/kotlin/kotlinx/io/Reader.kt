package kotlinx.io

import io.ktor.utils.io.core.Closeable
import kotlin.math.min

/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

/**
 * Abstract class for reading character streams.  The only methods that a
 * subclass must implement are read(char[], int, int) and close().  Most
 * subclasses, however, will override some of the methods defined here in order
 * to provide higher efficiency, additional functionality, or both.
 *
 *
 * @see BufferedReader
 *
 * @see LineNumberReader
 *
 * @see CharArrayReader
 *
 * @see InputStreamReader
 *
 * @see FileReader
 *
 * @see FilterReader
 *
 * @see PushbackReader
 *
 * @see PipedReader
 *
 * @see StringReader
 *
 * @see Writer
 *
 *
 * @author      Mark Reinhold
 * @since       JDK1.1
 */
abstract class Reader : Readable, Closeable {

    /**
     * The object used to synchronize operations on this stream.  For
     * efficiency, a character-stream object may use an object other than
     * itself to protect critical sections.  A subclass should therefore use
     * the object in this field rather than <tt>this</tt> or a synchronized
     * method.
     */
    protected var lock: Any

    /**
     * Creates a new character-stream reader whose critical sections will
     * synchronize on the reader itself.
     */
    protected constructor() {
        lock = this
    }

    /**
     * Creates a new character-stream reader whose critical sections will
     * synchronize on the given object.
     *
     * @param lock  The Object to synchronize on.
     */
    protected constructor(lock: Any?) {
        if (lock == null) {
            throw NullPointerException()
        }
        this.lock = lock
    }

    /**
     * Reads a single character.  This method will block until a character is
     * available, an I/O error occurs, or the end of the stream is reached.
     *
     *
     *  Subclasses that intend to support efficient single-character input
     * should override this method.
     *
     * @return     The character read, as an integer in the range 0 to 65535
     * (<tt>0x00-0xffff</tt>), or -1 if the end of the stream has
     * been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Throws(IOException::class)
    open fun read(): Int {
        val cb = CharArray(1)
        return if (read(cb, 0, 1) == -1) -1 else cb[0].toInt()
    }

    /**
     * Reads characters into an array.  This method will block until some input
     * is available, an I/O error occurs, or the end of the stream is reached.
     *
     * @param       cbuf  Destination buffer
     *
     * @return      The number of characters read, or -1
     * if the end of the stream
     * has been reached
     *
     * @exception   IOException  If an I/O error occurs
     */
    @Throws(IOException::class)
    override fun read(cbuf: CharArray): Int {
        return read(cbuf, 0, cbuf.size)
    }

    /**
     * Reads characters into a portion of an array.  This method will block
     * until some input is available, an I/O error occurs, or the end of the
     * stream is reached.
     *
     * @param      cbuf  Destination buffer
     * @param      off   Offset at which to start storing characters
     * @param      len   Maximum number of characters to read
     *
     * @return     The number of characters read, or -1 if the end of the
     * stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Throws(IOException::class)
    abstract fun read(cbuf: CharArray, off: Int, len: Int): Int

    /** Skip buffer, null until allocated  */
    private var skipBuffer: CharArray? = null

    /**
     * Skips characters.  This method will block until some characters are
     * available, an I/O error occurs, or the end of the stream is reached.
     *
     * @param  n  The number of characters to skip
     *
     * @return    The number of characters actually skipped
     *
     * @exception  IllegalArgumentException  If `n` is negative.
     * @exception  IOException  If an I/O error occurs
     */
    @Throws(IOException::class)
    open fun skip(n: Long): Long {
        require(n >= 0L) { "skip value is negative" }
        val nn = min(n, maxSkipBufferSize.toLong()).toInt()
//        synchronized(lock) {
            if (skipBuffer == null || skipBuffer!!.size < nn) skipBuffer = CharArray(nn)
            var r = n
            while (r > 0) {
                val nc = read(skipBuffer!!, 0, min(r, nn.toLong()).toInt())
                if (nc == -1) break
                r -= nc.toLong()
            }
            return n - r
//        }
    }

    /**
     * Tells whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input,
     * false otherwise.  Note that returning false does not guarantee that the
     * next read will block.
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Throws(IOException::class)
    open fun ready(): Boolean {
        return false
    }

    /**
     * Tells whether this stream supports the mark() operation. The default
     * implementation always returns false. Subclasses should override this
     * method.
     *
     * @return true if and only if this stream supports the mark operation.
     */
    open fun markSupported(): Boolean {
        return false
    }

    /**
     * Marks the present position in the stream.  Subsequent calls to reset()
     * will attempt to reposition the stream to this point.  Not all
     * character-input streams support the mark() operation.
     *
     * @param  readAheadLimit  Limit on the number of characters that may be
     * read while still preserving the mark.  After
     * reading this many characters, attempting to
     * reset the stream may fail.
     *
     * @exception  IOException  If the stream does not support mark(),
     * or if some other I/O error occurs
     */
    @Throws(IOException::class)
    open fun mark(readAheadLimit: Int) {
        throw IOException("mark() not supported")
    }

    /**
     * Resets the stream.  If the stream has been marked, then attempt to
     * reposition it at the mark.  If the stream has not been marked, then
     * attempt to reset it in some way appropriate to the particular stream,
     * for example by repositioning it to its starting point.  Not all
     * character-input streams support the reset() operation, and some support
     * reset() without supporting mark().
     *
     * @exception  IOException  If the stream has not been marked,
     * or if the mark has been invalidated,
     * or if the stream does not support reset(),
     * or if some other I/O error occurs
     */
    @Throws(IOException::class)
    open fun reset() {
        throw IOException("reset() not supported")
    }

    /**
     * Closes the stream and releases any system resources associated with
     * it.  Once the stream has been closed, further read(), ready(),
     * mark(), reset(), or skip() invocations will throw an IOException.
     * Closing a previously closed stream has no effect.
     *
     * @exception  IOException  If an I/O error occurs
     */
    abstract override fun close()

    companion object {

        /** Maximum skip-buffer size  */
        private const val maxSkipBufferSize = 8192
    }
}
