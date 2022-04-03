package kotlinx.io


/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

/**
 * A <tt>Readable</tt> is a source of characters. Characters from
 * a <tt>Readable</tt> are made available to callers of the read
 * method via a [CharBuffer][java.nio.CharBuffer].
 *
 * @since 1.5
 */
interface Readable {

    /**
     * Attempts to read characters into the specified character buffer.
     * The buffer is used as a repository of characters as-is: the only
     * changes made are the results of a put operation. No flipping or
     * rewinding of the buffer is performed.
     *
     * @param cb the buffer to read characters into
     * @return The number of `char` values added to the buffer,
     * or -1 if this source of characters is at its end
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if cb is null
     * @throws java.nio.ReadOnlyBufferException if cb is a read only buffer
     */
    @Throws(IOException::class)
    fun read(cb: CharArray): Int
}
