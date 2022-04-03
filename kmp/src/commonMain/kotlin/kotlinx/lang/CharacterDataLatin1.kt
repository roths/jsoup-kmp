package kotlinx.lang
import kotlinx.*

/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

/** The CharacterData class encapsulates the large tables found in
 * Java.lang.Character.  */
internal class CharacterDataLatin1 private constructor() : CharacterData() {

    /* The character properties are currently encoded into 32 bits in the following manner:
        1 bit   mirrored property
        4 bits  directionality property
        9 bits  signed offset used for converting case
        1 bit   if 1, adding the signed offset converts the character to lowercase
        1 bit   if 1, subtracting the signed offset converts the character to uppercase
        1 bit   if 1, this character has a titlecase equivalent (possibly itself)
        3 bits  0  may not be part of an identifier
                1  ignorable control; may continue a Unicode identifier or Java identifier
                2  may continue a Java identifier but not a Unicode identifier (unused)
                3  may continue a Unicode identifier or Java identifier
                4  is a Java whitespace character
                5  may start or continue a Java identifier;
                   may continue but not start a Unicode identifier (underscores)
                6  may start or continue a Java identifier but not a Unicode identifier ($)
                7  may start or continue a Unicode identifier or Java identifier
                Thus:
                   5, 6, 7 may start a Java identifier
                   1, 2, 3, 5, 6, 7 may continue a Java identifier
                   7 may start a Unicode identifier
                   1, 3, 5, 7 may continue a Unicode identifier
                   1 is ignorable within an identifier
                   4 is Java whitespace
        2 bits  0  this character has no numeric property
                1  adding the digit offset to the character code and then
                   masking with 0x1F will produce the desired numeric value
                2  this character has a "strange" numeric value
                3  a Java supradecimal digit: adding the digit offset to the
                   character code, then masking with 0x1F, then adding 10
                   will produce the desired numeric value
        5 bits  digit offset
        5 bits  character type

        The encoding of character properties is subject to change at any time.
     */
    override fun getProperties(ch: Int): Int {
        val offset = ch.toChar()
        return A[offset.toInt()]
    }

    fun getPropertiesEx(ch: Int): Int {
        val offset = ch.toChar()
        return B[offset.toInt()].toInt()
    }

    override fun isOtherLowercase(ch: Int): Boolean {
        val props = getPropertiesEx(ch)
        return props and 0x0001 != 0
    }

    override fun isOtherUppercase(ch: Int): Boolean {
        val props = getPropertiesEx(ch)
        return props and 0x0002 != 0
    }

    override fun isOtherAlphabetic(ch: Int): Boolean {
        val props = getPropertiesEx(ch)
        return props and 0x0004 != 0
    }

    override fun isIdeographic(ch: Int): Boolean {
        val props = getPropertiesEx(ch)
        return props and 0x0010 != 0
    }

    override fun getType(ch: Int): Int {
        val props = getProperties(ch)
        return props and 0x1F
    }

    override fun isJavaIdentifierStart(ch: Int): Boolean {
        val props = getProperties(ch)
        return props and 0x00007000 >= 0x00005000
    }

    override fun isJavaIdentifierPart(ch: Int): Boolean {
        val props = getProperties(ch)
        return props and 0x00003000 != 0
    }

    override fun isUnicodeIdentifierStart(ch: Int): Boolean {
        val props = getProperties(ch)
        return props and 0x00007000 == 0x00007000
    }

    override fun isUnicodeIdentifierPart(ch: Int): Boolean {
        val props = getProperties(ch)
        return props and 0x00001000 != 0
    }

    override fun isIdentifierIgnorable(ch: Int): Boolean {
        val props = getProperties(ch)
        return props and 0x00007000 == 0x00001000
    }

    override fun toLowerCase(ch: Int): Int {
        var mapChar = ch
        val `val` = getProperties(ch)
        if (`val` and 0x00020000 != 0 &&
            `val` and 0x07FC0000 != 0x07FC0000
        ) {
            val offset = `val` shl 5 shr 5 + 18
            mapChar = ch + offset
        }
        return mapChar
    }

    override fun toUpperCase(ch: Int): Int {
        var mapChar = ch
        val `val` = getProperties(ch)
        if (`val` and 0x00010000 != 0) {
            if (`val` and 0x07FC0000 != 0x07FC0000) {
                val offset = `val` shl 5 shr 5 + 18
                mapChar = ch - offset
            } else if (ch == 0x00B5) {
                mapChar = 0x039C
            }
        }
        return mapChar
    }

    override fun toTitleCase(ch: Int): Int {
        return toUpperCase(ch)
    }

    override fun digit(ch: Int, radix: Int): Int {
        var value = -1
        if (radix >= Char.MIN_RADIX && radix <= Char.MAX_RADIX) {
            val `val` = getProperties(ch)
            val kind = `val` and 0x1F
            if (kind == Char.DECIMAL_DIGIT_NUMBER.toInt()) {
                value = ch + (`val` and 0x3E0 shr 5) and 0x1F
            } else if (`val` and 0xC00 == 0x00000C00) {
                // Java supradecimal digit
                value = (ch + (`val` and 0x3E0 shr 5) and 0x1F) + 10
            }
        }
        return if (value < radix) value else -1
    }

    override fun getNumericValue(ch: Int): Int {
        val `val` = getProperties(ch)
        var retval = -1
        retval = when (`val` and 0xC00) {
            0x00000000 -> -1
            0x00000400 -> ch + (`val` and 0x3E0 shr 5) and 0x1F
            0x00000800 -> -2
            0x00000C00 -> (ch + (`val` and 0x3E0 shr 5) and 0x1F) + 10
            else -> -1
        }
        return retval
    }

    override fun isWhitespace(ch: Int): Boolean {
        val props = getProperties(ch)
        return props and 0x00007000 == 0x00004000
    }

    override fun getDirectionality(ch: Int): Byte {
        val `val` = getProperties(ch)
        var directionality = (`val` and 0x78000000 shr 27).toByte()
        if (directionality.toInt() == 0xF) {
            directionality = -1
        }
        return directionality
    }

    override fun isMirrored(ch: Int): Boolean {
        val props = getProperties(ch)
        return props and -0x80000000 != 0
    }

    override fun toUpperCaseEx(ch: Int): Int {
        var mapChar = ch
        val `val` = getProperties(ch)
        if (`val` and 0x00010000 != 0) {
            if (`val` and 0x07FC0000 != 0x07FC0000) {
                val offset = `val` shl 5 shr 5 + 18
                mapChar = ch - offset
            } else {
                when (ch) {
                    0x00B5 -> mapChar = 0x039C
                    else -> mapChar = Char.ERROR
                }
            }
        }
        return mapChar
    }

    override fun toUpperCaseCharArray(ch: Int): CharArray {
        var upperMap = charArrayOf(ch.toChar())
        if (ch == 0x00DF) {
            upperMap = sharpsMap
        }
        return upperMap
    }

    companion object {

        var sharpsMap = charArrayOf('S', 'S')
        val instance = CharacterDataLatin1()

        // The following tables and code generated using:
        // java GenerateCharacter -template /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/characterdata/CharacterDataLatin1.java.template -spec /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/unicodedata/UnicodeData.txt -specialcasing /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/unicodedata/SpecialCasing.txt -proplist /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/unicodedata/PropList.txt -o /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/build/macosx-x86_64/jdk/gensrc/java/lang/CharacterDataLatin1.java -string -usecharforbyte -latin1 8
        // The A table has 256 entries for a total of 1024 bytes.
        val A = IntArray(256)
        const val A_DATA = "\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800" +
                "\u100F\u4800\u100F\u4800\u100F\u5800\u400F\u5000\u400F\u5800\u400F\u6000\u400F" +
                "\u5000\u400F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800" +
                "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F" +
                "\u4800\u100F\u4800\u100F\u5000\u400F\u5000\u400F\u5000\u400F\u5800\u400F\u6000" +
                "\u400C\u6800\u0018\u6800\u0018\u2800\u0018\u2800\u601A\u2800\u0018\u6800\u0018\u6800" +
                "\u0018\uE800\u0015\uE800\u0016\u6800\u0018\u2000\u0019\u3800\u0018\u2000\u0014\u3800\u0018" +
                "\u3800\u0018\u1800\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u1800" +
                "\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u1800\u3609\u3800\u0018\u6800\u0018" +
                "\uE800\u0019\u6800\u0019\uE800\u0019\u6800\u0018\u6800\u0018\u0082\u7FE1\u0082\u7FE1\u0082" +
                "\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1" +
                "\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082" +
                "\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1" +
                "\u0082\u7FE1\uE800\u0015\u6800\u0018\uE800\u0016\u6800\u001b\u6800\u5017\u6800\u001b\u0081" +
                "\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2" +
                "\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081" +
                "\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2" +
                "\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\uE800\u0015\u6800\u0019\uE800\u0016\u6800\u0019\u4800" +
                "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u5000\u100F" +
                "\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800" +
                "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F" +
                "\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800" +
                "\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F\u4800\u100F" +
                "\u3800\u000c\u6800\u0018\u2800\u601A\u2800\u601A\u2800\u601A\u2800\u601A\u6800" +
                "\u001c\u6800\u0018\u6800\u001b\u6800\u001c\u0000\u7005\uE800\u001d\u6800\u0019\u4800\u1010" +
                "\u6800\u001c\u6800\u001b\u2800\u001c\u2800\u0019\u1800\u060B\u1800\u060B\u6800\u001b" +
                "\u07FD\u7002\u6800\u0018\u6800\u0018\u6800\u001b\u1800\u050B\u0000\u7005\uE800\u001e" +
                "\u6800\u080B\u6800\u080B\u6800\u080B\u6800\u0018\u0082\u7001\u0082\u7001\u0082\u7001" +
                "\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082" +
                "\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001" +
                "\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u6800\u0019\u0082\u7001\u0082" +
                "\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u0082\u7001\u07FD\u7002\u0081\u7002" +
                "\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081" +
                "\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002" +
                "\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u6800" +
                "\u0019\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u0081\u7002" +
                "\u061D\u7002"

        // The B table has 256 entries for a total of 512 bytes.
        val B =
            ("\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000").toCharArray()

        // In all, the character property tables require 1024 bytes.
        init {
            // THIS CODE WAS AUTOMATICALLY CREATED BY GenerateCharacter:
            val data = A_DATA.toCharArray()
            if (data.size != 256 * 2) {
                throw RuntimeException("error")
            }
            var i = 0
            var j = 0
            while (i < 256 * 2) {
                val entry: Int = data[i++].code shl 16
                A[j++] = entry or data[i++].code
            }
        }
    }
}