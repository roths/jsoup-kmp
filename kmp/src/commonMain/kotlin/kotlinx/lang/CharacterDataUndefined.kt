package kotlinx.lang
import kotlinx.*

/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

/** The CharacterData class encapsulates the large tables found in
 * Java.lang.Character.  */
internal class CharacterDataUndefined private constructor() : CharacterData() {

    override fun getProperties(ch: Int): Int {
        return 0
    }

    override fun getType(ch: Int): Int {
        return Char.UNASSIGNED.toInt()
    }

    override fun isJavaIdentifierStart(ch: Int): Boolean {
        return false
    }

    override fun isJavaIdentifierPart(ch: Int): Boolean {
        return false
    }

    override fun isUnicodeIdentifierStart(ch: Int): Boolean {
        return false
    }

    override fun isUnicodeIdentifierPart(ch: Int): Boolean {
        return false
    }

    override fun isIdentifierIgnorable(ch: Int): Boolean {
        return false
    }

    override fun toLowerCase(ch: Int): Int {
        return ch
    }

    override fun toUpperCase(ch: Int): Int {
        return ch
    }

    override fun toTitleCase(ch: Int): Int {
        return ch
    }

    override fun digit(ch: Int, radix: Int): Int {
        return -1
    }

    override fun getNumericValue(ch: Int): Int {
        return -1
    }

    override fun isWhitespace(ch: Int): Boolean {
        return false
    }

    override fun getDirectionality(ch: Int): Byte {
        return Char.DIRECTIONALITY_UNDEFINED
    }

    override fun isMirrored(ch: Int): Boolean {
        return false
    }

    companion object {

        val instance = CharacterDataUndefined()
    }
}
