package kotlinx.lang

/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

internal abstract class CharacterData {

    abstract fun getProperties(ch: Int): Int
    abstract fun getType(ch: Int): Int
    abstract fun isWhitespace(ch: Int): Boolean
    abstract fun isMirrored(ch: Int): Boolean
    abstract fun isJavaIdentifierStart(ch: Int): Boolean
    abstract fun isJavaIdentifierPart(ch: Int): Boolean
    abstract fun isUnicodeIdentifierStart(ch: Int): Boolean
    abstract fun isUnicodeIdentifierPart(ch: Int): Boolean
    abstract fun isIdentifierIgnorable(ch: Int): Boolean
    abstract fun toLowerCase(ch: Int): Int
    abstract fun toUpperCase(ch: Int): Int
    abstract fun toTitleCase(ch: Int): Int
    abstract fun digit(ch: Int, radix: Int): Int
    abstract fun getNumericValue(ch: Int): Int
    abstract fun getDirectionality(ch: Int): Byte

    //need to implement for JSR204
    open fun toUpperCaseEx(ch: Int): Int {
        return toUpperCase(ch)
    }

    open fun toUpperCaseCharArray(ch: Int): CharArray? {
        return null
    }

    open fun isOtherLowercase(ch: Int): Boolean {
        return false
    }

    open fun isOtherUppercase(ch: Int): Boolean {
        return false
    }

    open fun isOtherAlphabetic(ch: Int): Boolean {
        return false
    }

    open fun isIdeographic(ch: Int): Boolean {
        return false
    }

    companion object {

        // Character <= 0xff (basic latin) is handled by internal fast-path
        // to avoid initializing large tables.
        // Note: performance of this "fast-path" code may be sub-optimal
        // in negative cases for some accessors due to complicated ranges.
        // Should revisit after optimization of table initialization.
        fun of(ch: Int): CharacterData {
            return if (ch ushr 8 == 0) {     // fast-path
                CharacterDataLatin1.instance
            } else {
                when (ch ushr 16) {
                    0 -> CharacterData00.instance
                    1 -> CharacterData01.instance
                    2 -> CharacterData02.instance
                    14 -> CharacterData0E.instance
                    15, 16 -> CharacterDataPrivateUse.instance
                    else -> CharacterDataUndefined.instance
                }
            }
        }
    }
}
