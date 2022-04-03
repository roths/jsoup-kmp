package kotlinx.lang

import kotlinx.*

/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

/**
 * The CharacterData00 class encapsulates the large tables once found in
 * java.lang.Character
 */
internal class CharacterData00 private constructor() : CharacterData() {

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
        return A[Y[X[offset.code shr 5].code or (offset.code shr 1 and 0xF)].code or (offset.code and 0x1)]
    }

    fun getPropertiesEx(ch: Int): Int {
        val offset = ch.toChar()
        return B[Y[X[offset.code shr 5].code or (offset.code shr 1 and 0xF)].code or (offset.code and 0x1)]
            .toInt()
    }

    override fun getType(ch: Int): Int {
        val props = getProperties(ch)
        return props and 0x1F
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
        if (`val` and 0x00020000 != 0) {
            if (`val` and 0x07FC0000 == 0x07FC0000) {
                when (ch) {
                    0x0130 -> mapChar = 0x0069
                    0x2126 -> mapChar = 0x03C9
                    0x212A -> mapChar = 0x006B
                    0x212B -> mapChar = 0x00E5
                    0x1F88 -> mapChar = 0x1F80
                    0x1F89 -> mapChar = 0x1F81
                    0x1F8A -> mapChar = 0x1F82
                    0x1F8B -> mapChar = 0x1F83
                    0x1F8C -> mapChar = 0x1F84
                    0x1F8D -> mapChar = 0x1F85
                    0x1F8E -> mapChar = 0x1F86
                    0x1F8F -> mapChar = 0x1F87
                    0x1F98 -> mapChar = 0x1F90
                    0x1F99 -> mapChar = 0x1F91
                    0x1F9A -> mapChar = 0x1F92
                    0x1F9B -> mapChar = 0x1F93
                    0x1F9C -> mapChar = 0x1F94
                    0x1F9D -> mapChar = 0x1F95
                    0x1F9E -> mapChar = 0x1F96
                    0x1F9F -> mapChar = 0x1F97
                    0x1FA8 -> mapChar = 0x1FA0
                    0x1FA9 -> mapChar = 0x1FA1
                    0x1FAA -> mapChar = 0x1FA2
                    0x1FAB -> mapChar = 0x1FA3
                    0x1FAC -> mapChar = 0x1FA4
                    0x1FAD -> mapChar = 0x1FA5
                    0x1FAE -> mapChar = 0x1FA6
                    0x1FAF -> mapChar = 0x1FA7
                    0x1FBC -> mapChar = 0x1FB3
                    0x1FCC -> mapChar = 0x1FC3
                    0x1FFC -> mapChar = 0x1FF3
                    0x023A -> mapChar = 0x2C65
                    0x023E -> mapChar = 0x2C66
                    0x10A0 -> mapChar = 0x2D00
                    0x10A1 -> mapChar = 0x2D01
                    0x10A2 -> mapChar = 0x2D02
                    0x10A3 -> mapChar = 0x2D03
                    0x10A4 -> mapChar = 0x2D04
                    0x10A5 -> mapChar = 0x2D05
                    0x10A6 -> mapChar = 0x2D06
                    0x10A7 -> mapChar = 0x2D07
                    0x10A8 -> mapChar = 0x2D08
                    0x10A9 -> mapChar = 0x2D09
                    0x10AA -> mapChar = 0x2D0A
                    0x10AB -> mapChar = 0x2D0B
                    0x10AC -> mapChar = 0x2D0C
                    0x10AD -> mapChar = 0x2D0D
                    0x10AE -> mapChar = 0x2D0E
                    0x10AF -> mapChar = 0x2D0F
                    0x10B0 -> mapChar = 0x2D10
                    0x10B1 -> mapChar = 0x2D11
                    0x10B2 -> mapChar = 0x2D12
                    0x10B3 -> mapChar = 0x2D13
                    0x10B4 -> mapChar = 0x2D14
                    0x10B5 -> mapChar = 0x2D15
                    0x10B6 -> mapChar = 0x2D16
                    0x10B7 -> mapChar = 0x2D17
                    0x10B8 -> mapChar = 0x2D18
                    0x10B9 -> mapChar = 0x2D19
                    0x10BA -> mapChar = 0x2D1A
                    0x10BB -> mapChar = 0x2D1B
                    0x10BC -> mapChar = 0x2D1C
                    0x10BD -> mapChar = 0x2D1D
                    0x10BE -> mapChar = 0x2D1E
                    0x10BF -> mapChar = 0x2D1F
                    0x10C0 -> mapChar = 0x2D20
                    0x10C1 -> mapChar = 0x2D21
                    0x10C2 -> mapChar = 0x2D22
                    0x10C3 -> mapChar = 0x2D23
                    0x10C4 -> mapChar = 0x2D24
                    0x10C5 -> mapChar = 0x2D25
                    0x10C7 -> mapChar = 0x2D27
                    0x10CD -> mapChar = 0x2D2D
                    0x1E9E -> mapChar = 0x00DF
                    0x2C62 -> mapChar = 0x026B
                    0x2C63 -> mapChar = 0x1D7D
                    0x2C64 -> mapChar = 0x027D
                    0x2C6D -> mapChar = 0x0251
                    0x2C6E -> mapChar = 0x0271
                    0x2C6F -> mapChar = 0x0250
                    0x2C70 -> mapChar = 0x0252
                    0x2C7E -> mapChar = 0x023F
                    0x2C7F -> mapChar = 0x0240
                    0xA77D -> mapChar = 0x1D79
                    0xA78D -> mapChar = 0x0265
                    0xA7AA -> mapChar = 0x0266
                }
            } else {
                val offset = `val` shl 5 shr 5 + 18
                mapChar = ch + offset
            }
        }
        return mapChar
    }

    override fun toUpperCase(ch: Int): Int {
        var mapChar = ch
        val `val` = getProperties(ch)
        if (`val` and 0x00010000 != 0) {
            if (`val` and 0x07FC0000 == 0x07FC0000) {
                when (ch) {
                    0x00B5 -> mapChar = 0x039C
                    0x017F -> mapChar = 0x0053
                    0x1FBE -> mapChar = 0x0399
                    0x1F80 -> mapChar = 0x1F88
                    0x1F81 -> mapChar = 0x1F89
                    0x1F82 -> mapChar = 0x1F8A
                    0x1F83 -> mapChar = 0x1F8B
                    0x1F84 -> mapChar = 0x1F8C
                    0x1F85 -> mapChar = 0x1F8D
                    0x1F86 -> mapChar = 0x1F8E
                    0x1F87 -> mapChar = 0x1F8F
                    0x1F90 -> mapChar = 0x1F98
                    0x1F91 -> mapChar = 0x1F99
                    0x1F92 -> mapChar = 0x1F9A
                    0x1F93 -> mapChar = 0x1F9B
                    0x1F94 -> mapChar = 0x1F9C
                    0x1F95 -> mapChar = 0x1F9D
                    0x1F96 -> mapChar = 0x1F9E
                    0x1F97 -> mapChar = 0x1F9F
                    0x1FA0 -> mapChar = 0x1FA8
                    0x1FA1 -> mapChar = 0x1FA9
                    0x1FA2 -> mapChar = 0x1FAA
                    0x1FA3 -> mapChar = 0x1FAB
                    0x1FA4 -> mapChar = 0x1FAC
                    0x1FA5 -> mapChar = 0x1FAD
                    0x1FA6 -> mapChar = 0x1FAE
                    0x1FA7 -> mapChar = 0x1FAF
                    0x1FB3 -> mapChar = 0x1FBC
                    0x1FC3 -> mapChar = 0x1FCC
                    0x1FF3 -> mapChar = 0x1FFC
                    0x023F -> mapChar = 0x2C7E
                    0x0240 -> mapChar = 0x2C7F
                    0x0250 -> mapChar = 0x2C6F
                    0x0251 -> mapChar = 0x2C6D
                    0x0252 -> mapChar = 0x2C70
                    0x0265 -> mapChar = 0xA78D
                    0x0266 -> mapChar = 0xA7AA
                    0x026B -> mapChar = 0x2C62
                    0x0271 -> mapChar = 0x2C6E
                    0x027D -> mapChar = 0x2C64
                    0x1D79 -> mapChar = 0xA77D
                    0x1D7D -> mapChar = 0x2C63
                    0x2C65 -> mapChar = 0x023A
                    0x2C66 -> mapChar = 0x023E
                    0x2D00 -> mapChar = 0x10A0
                    0x2D01 -> mapChar = 0x10A1
                    0x2D02 -> mapChar = 0x10A2
                    0x2D03 -> mapChar = 0x10A3
                    0x2D04 -> mapChar = 0x10A4
                    0x2D05 -> mapChar = 0x10A5
                    0x2D06 -> mapChar = 0x10A6
                    0x2D07 -> mapChar = 0x10A7
                    0x2D08 -> mapChar = 0x10A8
                    0x2D09 -> mapChar = 0x10A9
                    0x2D0A -> mapChar = 0x10AA
                    0x2D0B -> mapChar = 0x10AB
                    0x2D0C -> mapChar = 0x10AC
                    0x2D0D -> mapChar = 0x10AD
                    0x2D0E -> mapChar = 0x10AE
                    0x2D0F -> mapChar = 0x10AF
                    0x2D10 -> mapChar = 0x10B0
                    0x2D11 -> mapChar = 0x10B1
                    0x2D12 -> mapChar = 0x10B2
                    0x2D13 -> mapChar = 0x10B3
                    0x2D14 -> mapChar = 0x10B4
                    0x2D15 -> mapChar = 0x10B5
                    0x2D16 -> mapChar = 0x10B6
                    0x2D17 -> mapChar = 0x10B7
                    0x2D18 -> mapChar = 0x10B8
                    0x2D19 -> mapChar = 0x10B9
                    0x2D1A -> mapChar = 0x10BA
                    0x2D1B -> mapChar = 0x10BB
                    0x2D1C -> mapChar = 0x10BC
                    0x2D1D -> mapChar = 0x10BD
                    0x2D1E -> mapChar = 0x10BE
                    0x2D1F -> mapChar = 0x10BF
                    0x2D20 -> mapChar = 0x10C0
                    0x2D21 -> mapChar = 0x10C1
                    0x2D22 -> mapChar = 0x10C2
                    0x2D23 -> mapChar = 0x10C3
                    0x2D24 -> mapChar = 0x10C4
                    0x2D25 -> mapChar = 0x10C5
                    0x2D27 -> mapChar = 0x10C7
                    0x2D2D -> mapChar = 0x10CD
                }
            } else {
                val offset = `val` shl 5 shr 5 + 18
                mapChar = ch - offset
            }
        }
        return mapChar
    }

    override fun toTitleCase(ch: Int): Int {
        var mapChar = ch
        val `val` = getProperties(ch)
        if (`val` and 0x00008000 != 0) {
            // There is a titlecase equivalent.  Perform further checks:
            if (`val` and 0x00010000 == 0) {
                // The character does not have an uppercase equivalent, so it must
                // already be uppercase; so add 1 to get the titlecase form.
                mapChar = ch + 1
            } else if (`val` and 0x00020000 == 0) {
                // The character does not have a lowercase equivalent, so it must
                // already be lowercase; so subtract 1 to get the titlecase form.
                mapChar = ch - 1
            }
            // else {
            // The character has both an uppercase equivalent and a lowercase
            // equivalent, so it must itself be a titlecase form; return it.
            // return ch;
            //}
        } else if (`val` and 0x00010000 != 0) {
            // This character has no titlecase equivalent but it does have an
            // uppercase equivalent, so use that (subtract the signed case offset).
            mapChar = toUpperCase(ch)
        }
        return mapChar
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
            0x00000800 -> when (ch) {
                0x0BF1 -> 100
                0x0BF2 -> 1000
                0x1375 -> 40
                0x1376 -> 50
                0x1377 -> 60
                0x1378 -> 70
                0x1379 -> 80
                0x137A -> 90
                0x137B -> 100
                0x137C -> 10000
                0x215F -> 1
                0x216C -> 50
                0x216D -> 100
                0x216E -> 500
                0x216F -> 1000
                0x217C -> 50
                0x217D -> 100
                0x217E -> 500
                0x217F -> 1000
                0x2180 -> 1000
                0x2181 -> 5000
                0x2182 -> 10000
                0x324B -> 40
                0x324C -> 50
                0x324D -> 60
                0x324E -> 70
                0x324F -> 80
                0x325C -> 32
                0x325D -> 33
                0x325E -> 34
                0x325F -> 35
                0x32B1 -> 36
                0x32B2 -> 37
                0x32B3 -> 38
                0x32B4 -> 39
                0x32B5 -> 40
                0x32B6 -> 41
                0x32B7 -> 42
                0x32B8 -> 43
                0x32B9 -> 44
                0x32BA -> 45
                0x32BB -> 46
                0x32BC -> 47
                0x32BD -> 48
                0x32BE -> 49
                0x32BF -> 50
                0x0D71 -> 100
                0x0D72 -> 1000
                0x2186 -> 50
                0x2187 -> 50000
                0x2188 -> 100000
                else -> -2
            }
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
            directionality = when (ch) {
                0x202A ->                     // This is the only char with LRE
                    Char.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING
                0x202B ->                     // This is the only char with RLE
                    Char.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING
                0x202C ->                     // This is the only char with PDF
                    Char.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT
                0x202D ->                     // This is the only char with LRO
                    Char.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE
                0x202E ->                     // This is the only char with RLO
                    Char.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE
                else -> Char.DIRECTIONALITY_UNDEFINED
            }
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
                    0x017F -> mapChar = 0x0053
                    0x1FBE -> mapChar = 0x0399
                    0x023F -> mapChar = 0x2C7E
                    0x0240 -> mapChar = 0x2C7F
                    0x0250 -> mapChar = 0x2C6F
                    0x0251 -> mapChar = 0x2C6D
                    0x0252 -> mapChar = 0x2C70
                    0x0265 -> mapChar = 0xA78D
                    0x0266 -> mapChar = 0xA7AA
                    0x026B -> mapChar = 0x2C62
                    0x0271 -> mapChar = 0x2C6E
                    0x027D -> mapChar = 0x2C64
                    0x1D79 -> mapChar = 0xA77D
                    0x1D7D -> mapChar = 0x2C63
                    0x2C65 -> mapChar = 0x023A
                    0x2C66 -> mapChar = 0x023E
                    0x2D00 -> mapChar = 0x10A0
                    0x2D01 -> mapChar = 0x10A1
                    0x2D02 -> mapChar = 0x10A2
                    0x2D03 -> mapChar = 0x10A3
                    0x2D04 -> mapChar = 0x10A4
                    0x2D05 -> mapChar = 0x10A5
                    0x2D06 -> mapChar = 0x10A6
                    0x2D07 -> mapChar = 0x10A7
                    0x2D08 -> mapChar = 0x10A8
                    0x2D09 -> mapChar = 0x10A9
                    0x2D0A -> mapChar = 0x10AA
                    0x2D0B -> mapChar = 0x10AB
                    0x2D0C -> mapChar = 0x10AC
                    0x2D0D -> mapChar = 0x10AD
                    0x2D0E -> mapChar = 0x10AE
                    0x2D0F -> mapChar = 0x10AF
                    0x2D10 -> mapChar = 0x10B0
                    0x2D11 -> mapChar = 0x10B1
                    0x2D12 -> mapChar = 0x10B2
                    0x2D13 -> mapChar = 0x10B3
                    0x2D14 -> mapChar = 0x10B4
                    0x2D15 -> mapChar = 0x10B5
                    0x2D16 -> mapChar = 0x10B6
                    0x2D17 -> mapChar = 0x10B7
                    0x2D18 -> mapChar = 0x10B8
                    0x2D19 -> mapChar = 0x10B9
                    0x2D1A -> mapChar = 0x10BA
                    0x2D1B -> mapChar = 0x10BB
                    0x2D1C -> mapChar = 0x10BC
                    0x2D1D -> mapChar = 0x10BD
                    0x2D1E -> mapChar = 0x10BE
                    0x2D1F -> mapChar = 0x10BF
                    0x2D20 -> mapChar = 0x10C0
                    0x2D21 -> mapChar = 0x10C1
                    0x2D22 -> mapChar = 0x10C2
                    0x2D23 -> mapChar = 0x10C3
                    0x2D24 -> mapChar = 0x10C4
                    0x2D25 -> mapChar = 0x10C5
                    0x2D27 -> mapChar = 0x10C7
                    0x2D2D -> mapChar = 0x10CD
                    else -> mapChar = Char.ERROR
                }
            }
        }
        return mapChar
    }

    override fun toUpperCaseCharArray(ch: Int): CharArray? {
        var upperMap = charArrayOf(ch.toChar())
        val location = findInCharMap(ch)
        if (location != -1) {
            upperMap = charMap[location][1]
        }
        return upperMap
    }

    /**
     * Finds the character in the uppercase mapping table.
     *
     * @param ch the `char` to search
     * @return the index location ch in the table or -1 if not found
     * @since 1.4
     */
    fun findInCharMap(ch: Int): Int {
        if (charMap.isEmpty()) {
            return -1
        }
        var top: Int
        var bottom: Int
        var current: Int
        bottom = 0
        top = charMap.size
        current = top / 2
        // invariant: top > current >= bottom && ch >= CharacterData.charMap[bottom][0]
        while (top - bottom > 1) {
            if (ch >= charMap[current][0][0].code) {
                bottom = current
            } else {
                top = current
            }
            current = (top + bottom) / 2
        }
        return if (ch == charMap[current][0][0].code) current else -1
    }

    companion object {

        val instance = CharacterData00()

        // The following tables and code generated using:
        // java GenerateCharacter -plane 0 -template /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/characterdata/CharacterData00.java.template -spec /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/unicodedata/UnicodeData.txt -specialcasing /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/unicodedata/SpecialCasing.txt -proplist /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/unicodedata/PropList.txt -o /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/build/macosx-x86_64/jdk/gensrc/java/lang/CharacterData00.java -string -usecharforbyte 11 4 1
        val charMap: Array<Array<CharArray>>

        // The X table has 2048 entries for a total of 4096 bytes.
        val X =
            ("\u0000\u0010\u0020\u0030\u0040\u0050\u0060\u0070\u0080\u0090\u00a0\u00b0\u00c0\u00d0\u00e0\u00f0\u0080\u0100" +
                    "\u0110\u0120\u0130\u0140\u0150\u0160\u0170\u0170\u0180\u0190\u01A0\u01B0\u01C0" +
                    "\u01D0\u01E0\u01F0\u0200\u0080\u0210\u0080\u0220\u0080\u0080\u0230\u0240\u0250\u0260" +
                    "\u0270\u0280\u0290\u02A0\u02B0\u02C0\u02D0\u02B0\u02B0\u02E0\u02F0\u0300\u0310" +
                    "\u0320\u02B0\u02B0\u0330\u0340\u0350\u0360\u0370\u0380\u0390\u0390\u03A0\u0390" +
                    "\u03B0\u03C0\u03D0\u03E0\u03F0\u0400\u0410\u0420\u0430\u0440\u0450\u0460\u0470" +
                    "\u0480\u0490\u04A0\u04B0\u0400\u04C0\u04D0\u04E0\u04F0\u0500\u0510\u0520\u0530" +
                    "\u0540\u0550\u0560\u0570\u0580\u0590\u05A0\u0570\u05B0\u05C0\u05D0\u05E0\u05F0" +
                    "\u0600\u0610\u0620\u0630\u0640\u0390\u0650\u0660\u0670\u0390\u0680\u0690\u06A0" +
                    "\u06B0\u06C0\u06D0\u06E0\u0390\u06F0\u0700\u0710\u0720\u0730\u0740\u0750\u0760" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u0770\u06F0\u0780" +
                    "\u0790\u07A0\u06F0\u07B0\u06F0\u07C0\u07D0\u07E0\u06F0\u06F0\u07F0\u0800\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u0810\u0820\u06F0\u06F0\u0830\u0840\u0850\u0860\u0870" +
                    "\u06F0\u0880\u0890\u08A0\u08B0\u06F0\u08C0\u08D0\u06F0\u08E0\u06F0\u08F0\u0900" +
                    "\u0910\u0920\u0930\u06F0\u0940\u0950\u0960\u0970\u06F0\u0980\u0990\u09A0\u09B0" +
                    "\u0390\u0390\u09C0\u09D0\u09E0\u09F0\u0A00\u0A10\u06F0\u0A20\u06F0\u0A30\u0A40" +
                    "\u0A50\u0390\u0390\u0A60\u0A70\u0A80\u0A90\u0AA0\u0AB0\u0AC0\u0AA0\u0170\u0AD0" +
                    "\u0080\u0080\u0080\u0080\u0AE0\u0080\u0080\u0080\u0AF0\u0B00\u0B10\u0B20\u0B30\u0B40\u0B50" +
                    "\u0B60\u0B70\u0B80\u0B90\u0BA0\u0BB0\u0BC0\u0BD0\u0BE0\u0BF0\u0C00\u0C10\u0C20" +
                    "\u0C30\u0C40\u0C50\u0C60\u0C70\u0C80\u0C90\u0CA0\u0CB0\u0CC0\u0CD0\u0CE0\u0CF0" +
                    "\u0D00\u0D10\u0D20\u0D30\u0D40\u0D50\u0D60\u0960\u0D70\u0D80\u0D90\u0DA0\u0DB0" +
                    "\u0DC0\u0DD0\u0960\u0960\u0960\u0960\u0960\u0DE0\u0DF0\u0E00\u0960\u0960\u0960" +
                    "\u0E10\u0960\u0E20\u0960\u0960\u0E30\u0960\u0960\u0E40\u0E50\u0960\u0E60\u0E70" +
                    "\u0D10\u0D10\u0D10\u0D10\u0D10\u0D10\u0D10\u0D10\u0E80\u0E80\u0E80\u0E80\u0E90" +
                    "\u0EA0\u0EB0\u0EC0\u0ED0\u0EE0\u0EF0\u0F00\u0F10\u0F20\u0F30\u0F40\u0960\u0F50" +
                    "\u0F60\u0390\u0390\u0390\u0390\u0390\u0F70\u0F80\u0F90\u0FA0\u0080\u0080\u0080\u0FB0" +
                    "\u0FC0\u0FD0\u06F0\u0FE0\u0FF0\u1000\u1000\u1010\u1020\u1030\u0390\u0390\u1040" +
                    "\u0960\u0960\u1050\u0960\u0960\u0960\u0960\u0960\u0960\u1060\u1070\u1080\u1090" +
                    "\u0620\u06F0\u10A0\u0800\u06F0\u10B0\u10C0\u10D0\u06F0\u06F0\u10E0\u10F0\u0960" +
                    "\u1100\u1110\u1120\u1130\u1140\u1120\u1150\u1160\u1170\u0D10\u0D10\u0D10\u1180" +
                    "\u0D10\u0D10\u1190\u11A0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11C0\u0960\u0960\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0" +
                    "\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11B0\u11D0\u0390\u11E0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u11F0\u0960\u1200\u0A50\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u1210\u1220\u0080\u1230\u1240\u06F0\u06F0" +
                    "\u1250\u1260\u1270\u0080\u1280\u1290\u12A0\u0390\u12B0\u12C0\u12D0\u06F0\u12E0" +
                    "\u12F0\u1300\u1310\u1320\u1330\u1340\u1350\u0900\u03C0\u1360\u1370\u0390\u06F0" +
                    "\u1380\u1390\u13A0\u06F0\u13B0\u13C0\u13D0\u13E0\u13F0\u0390\u0390\u0390\u0390" +
                    "\u06F0\u1400\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0\u06F0" +
                    "\u1410\u1420\u1430\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440" +
                    "\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440" +
                    "\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440" +
                    "\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440" +
                    "\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440\u1440" +
                    "\u1440\u1440\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u1450" +
                    "\u1450\u1450\u1450\u1450\u1450\u1450\u1450\u11B0\u11B0\u11B0\u1460\u11B0\u1470" +
                    "\u1480\u1490\u11B0\u11B0\u11B0\u14A0\u11B0\u11B0\u14B0\u0390\u14C0\u14D0\u14E0" +
                    "\u02B0\u02B0\u14F0\u1500\u02B0\u02B0\u02B0\u02B0\u02B0\u02B0\u02B0\u02B0\u02B0" +
                    "\u02B0\u1510\u1520\u02B0\u1530\u02B0\u1540\u1550\u1560\u1570\u1580\u1590\u02B0" +
                    "\u02B0\u02B0\u15A0\u15B0\u0020\u15C0\u15D0\u15E0\u15F0\u1600\u1610").toCharArray()

        // The Y table has 5664 entries for a total of 11328 bytes.
        val Y = """           
 "${"$"}${"$"}${"$"}${"$"}${"$"}${"$"}${"$"}${"$"}${"$"}${"$"}${"$"}$&(*,............024  6             8::<>@BDFHJLNPRTTTTTTTTTTTVTTTXZZZZZZZZZZZ\ZZZ^````````````````````````b```dfffffffh```````````````````````jffhl``nprtvxpz|`~````f``fffffff````````` `¢````¤`````````¦¨ª¬®°`````²´¶¸º¼¾ÀÂÄÆÂÈÂÊÌÂÎÐÒÔÖØÚÜÜÜÜÞàââÜääæææææâäääääääÜÜèäääêìääääääääîîîîîîîîîîîîîîîîîîðîîîîîîîîîîîîîîîîîîîîî``ê`òôöøòòäúüþĀĂĄTTTTTTTTĆTTTTĈĊČZZZZZZZZĎZZZZĐĒĔĖĘĚ````````````ĜĞĠĢĤ`ĦĨĪĪĪĪĪĪĪĪTTTTTTTTTTTTTTTTZZZZZZZZZZZZZZZZĬĬĬĬĬĬĬĬ`Įîîİ```````````ĲffffffĴ````````````òòòòĶĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĺļľľľŀłłłłłłłłłłłłłłłłłłńņňòŊŌîîîîîîîîîîîîîîîŎŎŎŎŎŎŎŐŒŔŎŒòòòòŖŖŖŖŖŖŖŖŖŖŖŖŖŘòòŖŚŜòòòòòŞŞŠŢŤŦŨŪŎŎŎŎŎŬòŮŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŲŰŰŰŰŴŎŎŎŎŎŎŶŎŎŎŸŸŸŸŸźżŰžŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰƀŎŎŎƂƄŶŎƆƈƊîŶŰŰƌƎŮŮŮŮŮŮŮƐŴŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŎŎŎŎŎŎŎŎîîîîîƒƔŰŰŰŰŰŰŰŰŰŰŰŰŎŎŎŎŎžòòòòòòòƖƖƖƖƖŖŖŖŖŖŖŖŖŖŖŖŖŖŖŖŖƘîîîîƚ<ƜòòŖŖŖŖŖŖŖŖŖŖŖŎîƞŎŎŎŎƞŎƞŎƠòƢƢƢƢƢƢƢŜŖŖŖŖŖŖŖŖŖŖŖŖƘîòŜòòòòòòòòòòòòòòòòƤŰŰŰŰŰƤòòòòòòòòòòòŎŎŎîîîŎŎŎŎŎŎŎƦŎƨƨƪƬƮŎŎŎƨƬưƬƲîŶŎŎľƴƴƴƴƴƶƸƺƬƸƼƸƼƸƼƼƼòòƪƬƮŎƦƾǀƾưƼòòòƾòòƸŎòƴƴƴƴƴ:ǂǂǄǆòòƺƨƸƼòƸƼƸƼƼƸƼòƒƬƮƦòƺƦƺƠòƺòòòƸƼƼòòòƴƴƴƴƴŎǈòòòòòƺƨƸƸƸƼƼƸòƪƬƮŎŎƺƨƾưòƼòòòòòòòŎòƴƴƴƴƴǊòòòòòòòƼƼƸòƪƮƮŎƦƾǀƾưòòòòƨòòƸŎòƴƴƴƴƴǌǂǂǂòòòòòǎƸƼòƼòƸƼƼòƸƼòƼòòòƬƨǀòƬǀƬưòƼòòƾòòòòòòòƴƴƴƴƴǐǒŪŪǔǖòòƾƬƸƼƼƼƸòƸŎƨƬǀŎƦŎƠòòòƺƦòòòŎòƴƴƴƴƴòòòòǘǘǚǜòƬƸƼƼƼƸòƪǞƬƬǀǠǀƬƠòòòƾǀòòòƼŎòƴƴƴƴƴƸƼòòòòòòƼƸƬƮŎƦƬǀƬưƼòòòƾòòòòŎòƴƴƴƴƴǐǂǂòǢòƬƸƼòƸƸòƼòƒòƾƬŎƦƦƬƬƬƬòòòòòòòòòƬǤòòòòòƸǈŎŎŎƦòŊǦîîŶǨǪǪǪǪǪľòòƸƼƼƸƼƼƸòòòƸƸƸƸòƸǈŎŎŎƺǎòƼǬîîŶòǪǪǪǪǪòǮǰľľľľľľľǲǲǰîǰǰǰǴǴǴǴǴǂǂǂǂǂĮĮĮǶƸƼòƺŎŎŎŎŎŎƨŎîǨîǈŎŎŎŎŎƺŎŎŎŎŎŎŎŎŎŎŎŎŎŎŎŎŎƦǰǰǰǰǸǰǰǺǰľľǲǰǼǤòòǾƮŎƨŎŎƠưȀƮǎǴǴǴǴǴľľľƬŎŎǎȂȄǾȂǶǶǈŎǎƨƮȆǶǶȈȊǪǪǪǪǪǶƮǰȌȌȌȌȌȌȌȌȌȌȌȌȌȌȌȌȌȌȌȎòòȎòȐȒƼòƼƼòƼòƼòƼƼòƼƼòƼŌŶľľľľȔȖȖȖȖȘȚǂǂǂȜòŪŪŪŪŪòòòƼòòòòòȞȐȠȢȤȦòȐľȨȪòòòòòòòƼŎƒòòòòòŎǨǤòòòòŎòòòòòòƼƼŎòòòòòòîƮŎŎŎƬƬƬƬƨưîîîîîľƶľǊƲòǴǴǴǴǴòòòȬȬȬȬȬòòòȮȰîȲǪǪǪǪǪòòòȴòòòòǈƼòòòòòòòƼòŎƨƬƮƨƬòòƬƨƬƬưîòòǖòƴƴƴƴƴòƼòòòòòòòƬƬƬƬƬƬƬƬȶƬòòòǪǪǪǪǪȸòŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪǈƨƬòľǾƨŎŎŎƦȀƨƮŎŎŎƨƬƬƮƠîîîƒŌǴǴǴǴǴòòòǪǪǪǪǪòòòľľľƶľľľòòòòòòòòòŎŎȶȀŎŎƨƨƬƬƨȄòòǪǪǪǪǪľľľǲǰǰǰǰĮîîîîǰǰǰǰǺòŎȶǾŎŎƬŎȈƬǪǪǪǪǪȀŎƬƮƮŎǶòòòòľľƬƬƬƬŎŎŎŎƬîòņľľǴǴǴǴǴòƸǪǪǪǪǪâââľľľľľòòòòîǨîîîîîîȺîîîƪƲƬƪƼòòòòÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜȼȾÂɀÜÜîîîƒòòòòòòòòòòîî```````````²²ɂɄɆɆɆɆɈɈɈɈɆɆɆòɈɈɈòɆɆɆɆɈɈɈɈɆɆɆɆɈɈɈɈɆɆɆòɈɈɈòɊɊɊɊɌɌɌɌɆɆɆɆɈɈɈɈɎɐɐɒɔɖɘò²²²²ɚɚɚɚ²²²²ɚɚɚɚ²²²²ɚɚɚɚɆ²ɜ²Ɉɞɠɢä²ɜ²ɤɤɠäɆ²ò²ɈɦɨäɆ²ɪ²Ɉɬɮäò²ɜ²ɰɲɠɴɶɶɶɸɶɺɼɾʀʀʀʂʄʂʄʆʈʈʊʌʌʎʐʒʔʖʘʚʜʖʞɼɼʠòòɼɼɼʢòHHHʤʦʨʪʪʪʪʪʤʦȦÜÜÜÜÜÜʬò:::::::::::::ʮòòòòòòòòòòîîîîîîʰİʲİʲîîîîîƒòòòòòòòŪʴŪʶŪʸĖĖʺʶŪʼĖĖŪŪŪʴʾʴȌĖˀĖ˂ÚŪĖŢʼ˄ŪˆPPPPPPPPˈˈˈˈˈˈˊˊˌˌˌˌˌˌˎˎː˒˔ː˖òòòŢŢ˘ŪŪŢŪŪ˘˄Ū˘ŪŪŪ˘ŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŢŪ˘˘ŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŢŢŢŢŢŢ˚˜Ţ˜˜˜Ţ˚˞˚Ţ˜˜˚˜Ţ˚˜˜˜˜ŢŢ˚˚˜˜˜˜˜˜˜˜ŢŢ˜˜ŢŢŢŢ˚˜˜˜˜Ţ˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˚˜ŢŢŢŢŢŢ˜Ţ˜˜˜˜˜˜˜˜˜ŢŢ˜ŢŢŢŢ˚˜˜Ţ˜ŢŢ˜˜˜˜˜˜˜˜˜˜˜˜Ţ˜˜˜˜˜˜˜˜ŪŪŪŪ˜˜ŪŪŪŪŪŪŪŪŪŪ˜ŪŪŪˠˢŪŪŪŪŪǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰˤ˘ŪŪŪŪŪŪŪŪŪŪŪ˦ŪŪ˄ŢŢŢŢŢŢŢŢŢŢŢŢŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŢŢŢŪŪŪŪŪŪŪŪŪòòòòòòŪŪŪǖòòòòòòòòòòòòŪŪŪŪŪǖòòòòòòòòòò˨˨˨˨˨˨˨˨˨˨˪˪˪˪˪˪˪˪˪˪ˬˬˬˬˬˬˬˬˬˬǰǰǰǰǰǰǰǰǰǰǰǰǰˮˮˮˮˮˮˮˮˮˮˮˮˮ˰˰˰˰˰˰˰˰˰˰˰˰˰˲˴˴˴˴˶˸˸˸˸˺ŪŪŪŪŪŪŪŪŪŪŪ˄ŪŪŪŪ˄ŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŢŢŢŢŪŪŪŪŪŪŪ˄ŪŪŪŪŪŪŪŪŪŪŪŪŪŪˤŪŪŪŪŪŪŪŪŪ˼ŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪŪ˾˾˾˾˾˨˨˨˨˨̀̀̀̀̀ŪŪŪŪŪŪ˚̂̄˜˚˜ŢŢ˚˜ŢŢ˜Ţ˜˜ŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢŢʦ̆̆̆̆̆̆̆̆̆̆̄˚˜˜˜˜˜˜˜˜˜˜ŢŢŢŢŢŢŢ˜˜˜Ţ˚ŢŢ˜˜˜ŢŢ˚˚˜Ţ˜ŢŢŢŢŢ˜˜˜ŢŢŢŢŢŢŢ˜˜˜˜˜˜˜˜˜˜˜Ţ˚˚˜ŢŢ˜ŢŢŢ˜ŢŢŢŢŢŢŢŢŢŢŢ˚ŢŢŢŢŢ˜ŢŢ˜˜˚˚Ţ˚˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜Ţ˜˜˜˜˚˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜˜ŢŢŢ˜˜ŢŢ˜Ţ˚Ţ˚˜˜˚ŢŪŪŪŪŪŪŪŪŢŢŢŢŢŢŢŢŢŢ˘˄ŢŢ̈òŪŪŪŪŪòòòĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĸĺłłłłłłłłłłłłłłłłłłłłłłł̊`Ȍª¬ff̌ȌɄ`dtÜȌ``̎ŪŪ̐f̒î`òò̔̖²²²²²²²²²²²²²²²²²²²̘òò̘òòòòļǤòòòòòòŌƼòòòòƼƼƼƼŎŎŎŎŎŎŎŎŎŎŎŎŎŎŎŎ̚̚ʐʒ̚̜Ȯ̚̚̞ʀòòŪŪŪŪŪŪŪŪŪŪŪŪŪ˼ŪŪŪŪŪŪŪŪŪŪŪŪòòòòòòŪŪŪŪŪŪŪŪŪŪŪòòòòòòòòòòòòòŪŪŪŪŪŪòò
̢̠Ū̨̤̦̪̪̪̪îîǶ̬ââŪ̮̰̲ŪƼŌ̴̶Ȓ̲âȒòòƸòƸƼǰ̸̸ǰǰǰǰǰƼòòŪŪòòòòòòǰǰǰǰǰǰǰǰǰǰǰǰǰǰˤǖ̺̺̺̺̺ǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰ̼̾ǂǂ̀͂͂͂͂͂PPǰǰǰǰǰǰǰǰǰǰǰǰǰǰŪ˦ǰǰǰǰǰǰǰǰ̈́PPPPPPPǰǰǰǰǰǰŪŪǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǺǰǰǰǰǰǰǰǰǰǰǰˤŪ˦ǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰŪǰǰǰǰǰǰǰǰǰǰǰǰǰǰǰˤ͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆òòòòò͈͆͆͆͆͆͆òòòòòòòòòȴƼòŪŪŪŪŪŪŪŪŪŪŪǖòòòò͊ǴǴǴǴǴòòòòòòòòòò```````Ʋİ͌ŎŎŎŎî̞````````````òòòƺ͎͎͎͎͐îľľľòòòòäääääääääää͒ææææä``````````````````````ȼdf̌`````͔͖̌͘``òòòòòò`````͚òòòòòòòòòòòòòòòòòòòòòòÜƪƪƲǾƮƨŪŪòòǂǂǂǰ͜òòòòòòòƬƬƬƬƬƬƬƬƬƒòòòòľǪǪǪǪǪòòòîîîîîîîîîľȠòòǴǴǴǴǴŎŎƠîľǈŎŎŎŎŎȂòòòòòņƲƬŎŎƬƨƬ͞ľľľľľľļǪǪǪǪǪòòľǈŎŎƨƮƨƮƦòòòòǈƨòǪǪǪǪǪòľľȒǮǰȊòòǎŎǎǈǎƠƲƼòòòòòòòòòòòƸȴľǾŎƬľȴ͠ƒòòòòƸƼƸƼƸƼòòòòƼƼòòòòòòòòǾƮƬƨ͢ȈòǪǪǪǪǪòòòòòòòòòƼòƸòòͤͤͤͤͤͤͤͤͤͤͤͤͤͤͤͤͦͦͦͦͦͦͦͦͦͦͦͦͦͦͦͦ͆͆͆͆͆ͨ͆͆͆ͪ͆͆ͬ͆͆͆͆͆͆͆͆͆͆͆͆ͮ͆͆͆͆͆͆͆͆͆͆͆͆͆͆ͰͲ͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆ʹ͆͆͆͆͆͆͆͆ò͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆͆òòò²²²ɜòòòòò̘²²òòͶ͸ŖŖŖŖͺŖŖŖŖŖŖŘŖŖŘŘŖͶŘŖŖŖŖŖŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰͼͼͼͼͼͼͼͼòòòòòòòòƔŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰ;òòòòòòòòŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰòŰŰŰŰŰŰŰŰŰŰŰòòòòòòòòòòòòòòòòòòòòŰŰŰŰŰŰ΀òîîîîîîîî΂΄òòòîîîƒòòòò̜ΆΈΊΊΊΊΊΊΊ΄΂΄ʔΌΎΐΒ̆̆ΔΖ˜̈ΘʎòòŰŰƤŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰŰƤΚ̔ ,............02ʦʚȒâƼòòòòƼò:Μǔʮ˄Ţ˘ǖòòòòΞΠŪò""".toCharArray()

        // The A table has 930 entries for a total of 3720 bytes.
        val A = IntArray(930)
        const val A_DATA = "\u4800\u100F\u4800\u100F\u4800\u100F\u5800\u400F\u5000\u400F\u5800\u400F\u6000" +
                "\u400F\u5000\u400F\u5000\u400F\u5000\u400F\u6000\u400C\u6800\u0018\u6800\u0018" +
                "\u2800\u0018\u2800\u601A\u2800\u0018\u6800\u0018\u6800\u0018\uE800\u0015\uE800\u0016\u6800" +
                "\u0018\u2000\u0019\u3800\u0018\u2000\u0014\u3800\u0018\u3800\u0018\u1800\u3609\u1800\u3609" +
                "\u3800\u0018\u6800\u0018\uE800\u0019\u6800\u0019\uE800\u0019\u6800\u0018\u6800\u0018\u0082" +
                "\u7FE1\u0082\u7FE1\u0082\u7FE1\u0082\u7FE1\uE800\u0015\u6800\u0018\uE800\u0016\u6800\u001b" +
                "\u6800\u5017\u6800\u001b\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\u0081\u7FE2\uE800\u0015\u6800" +
                "\u0019\uE800\u0016\u6800\u0019\u4800\u100F\u4800\u100F\u5000\u100F\u3800\u000c\u6800" +
                "\u0018\u2800\u601A\u2800\u601A\u6800\u001c\u6800\u0018\u6800\u001b\u6800\u001c\u0000\u7005" +
                "\uE800\u001d\u6800\u0019\u4800\u1010\u6800\u001c\u6800\u001b\u2800\u001c\u2800\u0019\u1800" +
                "\u060B\u1800\u060B\u6800\u001b\u07FD\u7002\u6800\u001b\u1800\u050B\u0000\u7005\uE800" +
                "\u001e\u6800\u080B\u6800\u080B\u6800\u080B\u6800\u0018\u0082\u7001\u0082\u7001\u0082" +
                "\u7001\u6800\u0019\u0082\u7001\u07FD\u7002\u0081\u7002\u0081\u7002\u0081\u7002\u6800" +
                "\u0019\u0081\u7002\u061D\u7002\u0006\u7001\u0005\u7002\u07FF\uF001\u03A1\u7002\u0000" +
                "\u7002\u0006\u7001\u0005\u7002\u0006\u7001\u0005\u7002\u07FD\u7002\u061E\u7001\u0006" +
                "\u7001\u04F5\u7002\u034A\u7001\u033A\u7001\u0006\u7001\u0005\u7002\u0336\u7001" +
                "\u0336\u7001\u0006\u7001\u0005\u7002\u0000\u7002\u013E\u7001\u032A\u7001\u032E\u7001" +
                "\u0006\u7001\u033E\u7001\u067D\u7002\u034E\u7001\u0346\u7001\u0575\u7002\u0000" +
                "\u7002\u034E\u7001\u0356\u7001\u05F9\u7002\u035A\u7001\u036A\u7001\u0006\u7001" +
                "\u0005\u7002\u036A\u7001\u0000\u7002\u0000\u7002\u0005\u7002\u0366\u7001\u0366\u7001" +
                "\u0006\u7001\u0005\u7002\u036E\u7001\u0000\u7002\u0000\u7005\u0000\u7002\u0721\u7002" +
                "\u0000\u7005\u0000\u7005\u000a\uF001\u0007\uF003\u0009\uF002\u000a\uF001\u0007\uF003\u0009" +
                "\uF002\u0009\uF002\u0006\u7001\u0005\u7002\u013D\u7002\u07FD\u7002\u000a\uF001\u067E" +
                "\u7001\u0722\u7001\u05FA\u7001\u0000\u7002\u07FE\u7001\u0006\u7001\u0005\u7002\u0576" +
                "\u7001\u07FE\u7001\u07FD\u7002\u07FD\u7002\u0006\u7001\u0005\u7002\u04F6\u7001" +
                "\u0116\u7001\u011E\u7001\u07FD\u7002\u07FD\u7002\u07FD\u7002\u0349\u7002\u0339" +
                "\u7002\u0000\u7002\u0335\u7002\u0335\u7002\u0000\u7002\u0329\u7002\u0000\u7002\u032D" +
                "\u7002\u0335\u7002\u0000\u7002\u0000\u7002\u033D\u7002\u0000\u7002\u07FD\u7002\u07FD" +
                "\u7002\u0000\u7002\u0345\u7002\u034D\u7002\u0000\u7002\u034D\u7002\u0355\u7002" +
                "\u0000\u7002\u0000\u7002\u0359\u7002\u0369\u7002\u0000\u7002\u0000\u7002\u0369\u7002" +
                "\u0369\u7002\u0115\u7002\u0365\u7002\u0365\u7002\u011D\u7002\u0000\u7002\u036D" +
                "\u7002\u0000\u7002\u0000\u7005\u0000\u7002\u0000\u7004\u0000\u7004\u0000\u7004\u6800\u7004" +
                "\u6800\u7004\u0000\u7004\u0000\u7004\u0000\u7004\u6800\u001b\u6800\u001b\u6800\u7004" +
                "\u6800\u7004\u0000\u7004\u6800\u001b\u6800\u7004\u6800\u001b\u0000\u7004\u6800\u001b" +
                "\u4000\u3006\u4000\u3006\u4000\u3006\u46B1\u3006\u7800\u0000\u7800\u0000\u0000\u7004" +
                "\u05F9\u7002\u05F9\u7002\u05F9\u7002\u6800\u0018\u7800\u0000\u009a\u7001\u6800\u0018" +
                "\u0096\u7001\u0096\u7001\u0096\u7001\u7800\u0000\u0102\u7001\u7800\u0000\u00fe\u7001\u00fe" +
                "\u7001\u07FD\u7002\u0082\u7001\u7800\u0000\u0082\u7001\u0099\u7002\u0095\u7002\u0095\u7002" +
                "\u0095\u7002\u07FD\u7002\u0081\u7002\u007d\u7002\u0081\u7002\u0101\u7002\u00fd\u7002" +
                "\u00fd\u7002\u0022\u7001\u00f9\u7002\u00e5\u7002\u0000\u7001\u0000\u7001\u0000\u7001\u00bd" +
                "\u7002\u00d9\u7002\u0021\u7002\u0159\u7002\u0141\u7002\u07E5\u7002\u0000\u7002\u0712" +
                "\u7001\u0181\u7002\u6800\u0019\u0006\u7001\u0005\u7002\u07E6\u7001\u0000\u7002\u05FA" +
                "\u7001\u05FA\u7001\u05FA\u7001\u0142\u7001\u0142\u7001\u0141\u7002\u0141\u7002" +
                "\u0000\u001c\u4000\u3006\u4000\u0007\u4000\u0007\u003e\u7001\u0006\u7001\u0005\u7002\u003d" +
                "\u7002\u7800\u0000\u00c2\u7001\u00c2\u7001\u00c2\u7001\u00c2\u7001\u7800\u0000\u7800\u0000" +
                "\u0000\u7004\u0000\u0018\u0000\u0018\u7800\u0000\u00c1\u7002\u00c1\u7002\u00c1\u7002\u00c1\u7002" +
                "\u07FD\u7002\u7800\u0000\u0000\u0018\u6800\u0014\u7800\u0000\u7800\u0000\u2800\u601A\u7800" +
                "\u0000\u4000\u3006\u4000\u3006\u4000\u3006\u0800\u0014\u4000\u3006\u0800\u0018\u4000" +
                "\u3006\u4000\u3006\u0800\u0018\u0800\u7005\u0800\u7005\u0800\u7005\u7800\u0000" +
                "\u0800\u7005\u0800\u0018\u0800\u0018\u7800\u0000\u3000\u1010\u3000\u1010\u3000\u1010" +
                "\u7800\u0000\u6800\u0019\u6800\u0019\u1000\u0019\u2800\u0018\u2800\u0018\u1000\u601A\u3800" +
                "\u0018\u1000\u0018\u6800\u001c\u6800\u001c\u4000\u3006\u1000\u0018\u1000\u0018\u1000\u0018" +
                "\u1000\u7005\u1000\u7005\u1000\u7004\u1000\u7005\u1000\u7005\u4000\u3006\u4000" +
                "\u3006\u4000\u3006\u3000\u3409\u3000\u3409\u2800\u0018\u3000\u0018\u3000\u0018\u1000" +
                "\u0018\u4000\u3006\u1000\u7005\u1000\u0018\u1000\u7005\u4000\u3006\u3000\u1010" +
                "\u6800\u001c\u4000\u3006\u4000\u3006\u1000\u7004\u1000\u7004\u4000\u3006\u4000" +
                "\u3006\u6800\u001c\u1000\u7005\u1000\u001c\u1000\u001c\u1000\u7005\u7800\u0000\u1000" +
                "\u1010\u4000\u3006\u7800\u0000\u7800\u0000\u1000\u7005\u0800\u3409\u0800\u3409" +
                "\u0800\u7005\u4000\u3006\u0800\u7004\u0800\u7004\u0800\u7004\u7800\u0000\u0800" +
                "\u7004\u4000\u3006\u4000\u3006\u4000\u3006\u0800\u0018\u0800\u0018\u1000\u7005" +
                "\u7800\u0000\u4000\u3006\u7800\u0000\u4000\u3006\u0000\u3008\u4000\u3006\u0000\u7005" +
                "\u0000\u3008\u0000\u3008\u0000\u3008\u4000\u3006\u0000\u3008\u4000\u3006\u0000\u7005" +
                "\u4000\u3006\u0000\u3749\u0000\u3749\u0000\u0018\u0000\u7004\u7800\u0000\u0000\u7005\u7800" +
                "\u0000\u4000\u3006\u0000\u7005\u7800\u0000\u7800\u0000\u0000\u3008\u0000\u3008\u7800\u0000" +
                "\u0000\u080B\u0000\u080B\u0000\u080B\u0000\u06EB\u0000\u001c\u2800\u601A\u0000\u7005\u4000" +
                "\u3006\u0000\u0018\u2800\u601A\u0000\u001c\u0000\u7005\u4000\u3006\u0000\u7005\u0000\u074B" +
                "\u0000\u080B\u0000\u080B\u6800\u001c\u6800\u001c\u2800\u601A\u6800\u001c\u7800\u0000\u6800" +
                "\u050B\u6800\u050B\u6800\u04AB\u6800\u04AB\u6800\u04AB\u0000\u001c\u0000\u3008\u0000" +
                "\u3006\u0000\u3006\u0000\u3008\u7800\u0000\u0000\u001c\u0000\u0018\u7800\u0000\u0000\u7004\u4000" +
                "\u3006\u4000\u3006\u0000\u0018\u0000\u3609\u0000\u3609\u0000\u7004\u7800\u0000\u0000\u7005" +
                "\u0000\u001c\u0000\u001c\u0000\u001c\u0000\u0018\u0000\u001c\u0000\u3409\u0000\u3409\u0000\u3008\u0000" +
                "\u3008\u4000\u3006\u0000\u001c\u0000\u001c\u7800\u0000\u0000\u001c\u0000\u0018\u0000\u7005\u0000" +
                "\u3008\u4000\u3006\u0000\u3008\u0000\u3008\u0000\u3008\u0000\u3008\u0000\u7005\u4000" +
                "\u3006\u0000\u3008\u0000\u3008\u4000\u3006\u0000\u7005\u0000\u3008\u07FE\u7001\u07FE" +
                "\u7001\u7800\u0000\u07FE\u7001\u0000\u7005\u0000\u0018\u0000\u7004\u0000\u7005\u0000\u0018" +
                "\u0000\u070B\u0000\u070B\u0000\u070B\u0000\u070B\u0000\u042B\u0000\u054B\u0000\u080B\u0000" +
                "\u080B\u7800\u0000\u6800\u0014\u0000\u7005\u0000\u0018\u0000\u7005\u6000\u400C\u0000\u7005" +
                "\u0000\u7005\uE800\u0015\uE800\u0016\u7800\u0000\u0000\u746A\u0000\u746A\u0000\u746A\u7800" +
                "\u0000\u6800\u060B\u6800\u060B\u6800\u0014\u6800\u0018\u6800\u0018\u4000\u3006\u6000" +
                "\u400C\u7800\u0000\u0000\u7005\u0000\u7004\u0000\u3008\u0000\u7005\u0000\u04EB\u7800\u0000" +
                "\u4000\u3006\u0000\u3008\u0000\u7004\u0000\u7002\u0000\u7004\u07FD\u7002\u0000\u7002" +
                "\u0000\u7004\u07FD\u7002\u00ed\u7002\u07FE\u7001\u0000\u7002\u07E1\u7002\u07E1\u7002" +
                "\u07E2\u7001\u07E2\u7001\u07FD\u7002\u07E1\u7002\u7800\u0000\u07E2\u7001\u06D9" +
                "\u7002\u06D9\u7002\u06A9\u7002\u06A9\u7002\u0671\u7002\u0671\u7002\u0601\u7002" +
                "\u0601\u7002\u0641\u7002\u0641\u7002\u0609\u7002\u0609\u7002\u07FF\uF003\u07FF" +
                "\uF003\u07FD\u7002\u7800\u0000\u06DA\u7001\u06DA\u7001\u07FF\uF003\u6800\u001b" +
                "\u07FD\u7002\u6800\u001b\u06AA\u7001\u06AA\u7001\u0672\u7001\u0672\u7001\u7800" +
                "\u0000\u6800\u001b\u07FD\u7002\u07E5\u7002\u0642\u7001\u0642\u7001\u07E6\u7001" +
                "\u6800\u001b\u0602\u7001\u0602\u7001\u060A\u7001\u060A\u7001\u6800\u001b\u7800" +
                "\u0000\u6000\u400C\u6000\u400C\u6000\u400C\u6000\u000c\u6000\u400C\u4800\u1010" +
                "\u4800\u1010\u4800\u1010\u0000\u1010\u0800\u1010\u6800\u0014\u6800\u0014\u6800\u001d" +
                "\u6800\u001e\u6800\u0015\u6800\u001d\u6000\u400D\u5000\u400E\u7800\u1010\u7800\u1010" +
                "\u7800\u1010\u3800\u000c\u2800\u0018\u2800\u0018\u2800\u0018\u6800\u0018\u6800\u0018\uE800" +
                "\u001d\uE800\u001e\u6800\u0018\u6800\u0018\u6800\u5017\u6800\u5017\u6800\u0018\u3800" +
                "\u0019\uE800\u0015\uE800\u0016\u6800\u0018\u6800\u0019\u6800\u0018\u6800\u0018\u6000\u400C" +
                "\u4800\u1010\u7800\u0000\u1800\u060B\u0000\u7004\u2000\u0019\u2000\u0019\u6800\u0019" +
                "\uE800\u0015\uE800\u0016\u0000\u7004\u1800\u040B\u1800\u040B\u0000\u7004\u7800\u0000" +
                "\u2800\u601A\u7800\u0000\u4000\u3006\u4000\u0007\u4000\u0007\u4000\u3006\u0000\u7001" +
                "\u6800\u001c\u6800\u001c\u0000\u7001\u0000\u7002\u0000\u7001\u0000\u7001\u0000\u7002\u6800" +
                "\u0019\u0000\u7001\u07FE\u7001\u6800\u001c\u2800\u001c\u0000\u7002\u0072\u7001\u0000\u7001" +
                "\u6800\u001c\u6800\u0019\u0071\u7002\u0000\u001c\u0042\u742A\u0042\u742A\u0042\u780A\u0042\u780A" +
                "\u0041\u762A\u0041\u762A\u0041\u780A\u0041\u780A\u0000\u780A\u0000\u780A\u0000\u780A\u0006" +
                "\u7001\u0005\u7002\u0000\u742A\u0000\u780A\u6800\u06EB\u6800\u0019\u6800\u001c\u6800" +
                "\u0019\uE800\u0019\uE800\u0019\uE800\u0019\u2000\u0019\u2800\u0019\u6800\u001c\uE800\u0015" +
                "\uE800\u0016\u6800\u001c\u0000\u001c\u6800\u001c\u6800\u001c\u0000\u001c\u6800\u042B\u6800" +
                "\u042B\u6800\u05AB\u6800\u05AB\u1800\u072B\u1800\u072B\u006a\u001c\u006a\u001c\u0069" +
                "\u001c\u0069\u001c\u6800\u06CB\u6800\u040B\u6800\u040B\u6800\u040B\u6800\u040B\u6800" +
                "\u058B\u6800\u058B\u6800\u058B\u6800\u058B\u6800\u042B\u7800\u0000\u6800\u001c" +
                "\u6800\u056B\u6800\u056B\u6800\u06EB\u6800\u06EB\uE800\u0019\uE800\u0015\uE800" +
                "\u0016\u6800\u0019\uE800\u0016\uE800\u0015\u6800\u0019\u7800\u0000\u00c1\u7002\u7800\u0000" +
                "\u0005\u7002\u07FE\u7001\u0000\u7002\u6800\u001c\u6800\u001c\u0006\u7001\u0005\u7002\u4000" +
                "\u3006\u7800\u0000\u6800\u0018\u6800\u0018\u6800\u080B\u7800\u0000\u07FD\u7002\uE800" +
                "\u001d\uE800\u001e\u6800\u0018\u6800\u0014\u6800\u0018\u6800\u7004\u6800\u001c\u0000\u7004" +
                "\u0000\u7005\u0000\u772A\u6800\u0014\u6800\u0015\u6800\u0016\u6800\u0016\u6800\u001c\u0000" +
                "\u740A\u0000\u740A\u0000\u740A\u6800\u0014\u0000\u7004\u0000\u764A\u0000\u776A\u0000\u748A" +
                "\u0000\u7004\u0000\u7005\u6800\u0018\u4000\u3006\u6800\u001b\u6800\u001b\u0000\u7004\u0000" +
                "\u05EB\u0000\u05EB\u0000\u042B\u0000\u042B\u0000\u044B\u0000\u056B\u0000\u068B\u0000\u080B" +
                "\u6800\u001c\u6800\u048B\u6800\u048B\u6800\u048B\u0000\u001c\u6800\u080B\u0000\u7005" +
                "\u0000\u7005\u0000\u7005\u7800\u0000\u0000\u7004\u6800\u0018\u4000\u0007\u6800\u0018\u0000" +
                "\u776A\u0000\u776A\u0000\u776A\u0000\u762A\u6800\u001b\u6800\u7004\u6800\u7004\u0000" +
                "\u001b\u0000\u001b\u0006\u7001\u0000\u7002\u7800\u0000\u07FE\u7001\u7800\u0000\u2800\u601A" +
                "\u2800\u001c\u0000\u3008\u0000\u0018\u0000\u7004\u0000\u3008\u0000\u3008\u0000\u0018\u0000\u0013" +
                "\u0000\u0013\u0000\u0012\u0000\u0012\u0000\u7005\u0000\u7705\u0000\u7005\u0000\u76E5\u0000\u7545" +
                "\u0000\u7005\u0000\u75C5\u0000\u7005\u0000\u7005\u0000\u76A5\u0000\u7005\u0000\u7665\u0000" +
                "\u7005\u0000\u75A5\u7800\u0000\u0800\u7005\u4000\u3006\u0800\u7005\u0800\u7005" +
                "\u2000\u0019\u1000\u001b\u1000\u001b\u6800\u0015\u6800\u0016\u1000\u601A\u6800\u001c\u6800" +
                "\u0018\u6800\u0015\u6800\u0016\u6800\u0018\u6800\u0014\u6800\u5017\u6800\u5017\u6800" +
                "\u0015\u6800\u0016\u6800\u0015\u6800\u5017\u6800\u5017\u3800\u0018\u7800\u0000\u6800" +
                "\u0018\u3800\u0018\u6800\u0014\uE800\u0015\uE800\u0016\u2800\u0018\u2000\u0019\u2000\u0014" +
                "\u6800\u0018\u2800\u601A\u7800\u0000\u4800\u1010\u6800\u0019\u6800\u001b\u7800\u0000" +
                "\u6800\u1010\u6800\u1010\u6800\u1010"

        // The B table has 930 entries for a total of 1860 bytes.
        val B =
            ("\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0001\u0001\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0005\u0000\u0000\u0001\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0004\u0004\u0000\u0004\u0000\u0004\u0004\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0004\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0004\u0000\u0004\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0004\u0000\u0000\u0000\u0004\u0000\u0000\u0000\u0004\u0000\u0000\u0004\u0004\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0004\u0004\u0000" +
                    "\u0000\u0000\u0000\u0000\u0004\u0000\u0004\u0004\u0000\u0000\u0004\u0004\u0004\u0004\u0004\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0004\u0000\u0000\u0000\u0004\u0004\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0004\u0000\u0000\u0000\u0000\u0004\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0004\u0004\u0004\u0004\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0004\u0000" +
                    "\u0004\u0004\u0000\u0000\u0000\u0004\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0004\u0000\u0000\u0000" +
                    "\u0000\u0000\u0001\u0000\u0001\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0000\u0000\u0001\u0000\u0000" +
                    "\u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0002\u0002\u0002\u0002\u0001\u0001\u0001\u0001\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0006\u0006\u0005\u0005\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0010\u0010\u0000\u0000\u0000\u0000\u0000\u0010\u0010\u0010\u0000\u0000\u0010\u0010\u0010" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0010\u0010\u0010\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0004\u0004\u0000\u0000\u0000\u0000\u0000\u0010\u0010" +
                    "\u0010\u0010\u0010\u0010\u0010\u0010\u0010\u0010\u0010\u0010\u0010\u0010\u0000\u0000\u0004\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000").toCharArray()

        // In all, the character property tables require 19144 bytes.
        init {
            charMap = arrayOf(
                arrayOf(charArrayOf('\u00DF'), charArrayOf('\u0053', '\u0053')),
                arrayOf(charArrayOf('\u0130'), charArrayOf('\u0130')),
                arrayOf(charArrayOf('\u0149'), charArrayOf('\u02BC', '\u004E')),
                arrayOf(charArrayOf('\u01F0'), charArrayOf('\u004A', '\u030C')),
                arrayOf(charArrayOf('\u0390'), charArrayOf('\u0399', '\u0308', '\u0301')),
                arrayOf(charArrayOf('\u03B0'), charArrayOf('\u03A5', '\u0308', '\u0301')),
                arrayOf(charArrayOf('\u0587'), charArrayOf('\u0535', '\u0552')),
                arrayOf(charArrayOf('\u1E96'), charArrayOf('\u0048', '\u0331')),
                arrayOf(charArrayOf('\u1E97'), charArrayOf('\u0054', '\u0308')),
                arrayOf(charArrayOf('\u1E98'), charArrayOf('\u0057', '\u030A')),
                arrayOf(charArrayOf('\u1E99'), charArrayOf('\u0059', '\u030A')),
                arrayOf(charArrayOf('\u1E9A'), charArrayOf('\u0041', '\u02BE')),
                arrayOf(charArrayOf('\u1F50'), charArrayOf('\u03A5', '\u0313')),
                arrayOf(charArrayOf('\u1F52'), charArrayOf('\u03A5', '\u0313', '\u0300')),
                arrayOf(charArrayOf('\u1F54'), charArrayOf('\u03A5', '\u0313', '\u0301')),
                arrayOf(charArrayOf('\u1F56'), charArrayOf('\u03A5', '\u0313', '\u0342')),
                arrayOf(charArrayOf('\u1F80'), charArrayOf('\u1F08', '\u0399')),
                arrayOf(charArrayOf('\u1F81'), charArrayOf('\u1F09', '\u0399')),
                arrayOf(charArrayOf('\u1F82'), charArrayOf('\u1F0A', '\u0399')),
                arrayOf(charArrayOf('\u1F83'), charArrayOf('\u1F0B', '\u0399')),
                arrayOf(charArrayOf('\u1F84'), charArrayOf('\u1F0C', '\u0399')),
                arrayOf(charArrayOf('\u1F85'), charArrayOf('\u1F0D', '\u0399')),
                arrayOf(charArrayOf('\u1F86'), charArrayOf('\u1F0E', '\u0399')),
                arrayOf(charArrayOf('\u1F87'), charArrayOf('\u1F0F', '\u0399')),
                arrayOf(charArrayOf('\u1F88'), charArrayOf('\u1F08', '\u0399')),
                arrayOf(charArrayOf('\u1F89'), charArrayOf('\u1F09', '\u0399')),
                arrayOf(charArrayOf('\u1F8A'), charArrayOf('\u1F0A', '\u0399')),
                arrayOf(charArrayOf('\u1F8B'), charArrayOf('\u1F0B', '\u0399')),
                arrayOf(charArrayOf('\u1F8C'), charArrayOf('\u1F0C', '\u0399')),
                arrayOf(charArrayOf('\u1F8D'), charArrayOf('\u1F0D', '\u0399')),
                arrayOf(charArrayOf('\u1F8E'), charArrayOf('\u1F0E', '\u0399')),
                arrayOf(charArrayOf('\u1F8F'), charArrayOf('\u1F0F', '\u0399')),
                arrayOf(charArrayOf('\u1F90'), charArrayOf('\u1F28', '\u0399')),
                arrayOf(charArrayOf('\u1F91'), charArrayOf('\u1F29', '\u0399')),
                arrayOf(charArrayOf('\u1F92'), charArrayOf('\u1F2A', '\u0399')),
                arrayOf(charArrayOf('\u1F93'), charArrayOf('\u1F2B', '\u0399')),
                arrayOf(charArrayOf('\u1F94'), charArrayOf('\u1F2C', '\u0399')),
                arrayOf(charArrayOf('\u1F95'), charArrayOf('\u1F2D', '\u0399')),
                arrayOf(charArrayOf('\u1F96'), charArrayOf('\u1F2E', '\u0399')),
                arrayOf(charArrayOf('\u1F97'), charArrayOf('\u1F2F', '\u0399')),
                arrayOf(charArrayOf('\u1F98'), charArrayOf('\u1F28', '\u0399')),
                arrayOf(charArrayOf('\u1F99'), charArrayOf('\u1F29', '\u0399')),
                arrayOf(charArrayOf('\u1F9A'), charArrayOf('\u1F2A', '\u0399')),
                arrayOf(charArrayOf('\u1F9B'), charArrayOf('\u1F2B', '\u0399')),
                arrayOf(charArrayOf('\u1F9C'), charArrayOf('\u1F2C', '\u0399')),
                arrayOf(charArrayOf('\u1F9D'), charArrayOf('\u1F2D', '\u0399')),
                arrayOf(charArrayOf('\u1F9E'), charArrayOf('\u1F2E', '\u0399')),
                arrayOf(charArrayOf('\u1F9F'), charArrayOf('\u1F2F', '\u0399')),
                arrayOf(charArrayOf('\u1FA0'), charArrayOf('\u1F68', '\u0399')),
                arrayOf(charArrayOf('\u1FA1'), charArrayOf('\u1F69', '\u0399')),
                arrayOf(charArrayOf('\u1FA2'), charArrayOf('\u1F6A', '\u0399')),
                arrayOf(charArrayOf('\u1FA3'), charArrayOf('\u1F6B', '\u0399')),
                arrayOf(charArrayOf('\u1FA4'), charArrayOf('\u1F6C', '\u0399')),
                arrayOf(charArrayOf('\u1FA5'), charArrayOf('\u1F6D', '\u0399')),
                arrayOf(charArrayOf('\u1FA6'), charArrayOf('\u1F6E', '\u0399')),
                arrayOf(charArrayOf('\u1FA7'), charArrayOf('\u1F6F', '\u0399')),
                arrayOf(charArrayOf('\u1FA8'), charArrayOf('\u1F68', '\u0399')),
                arrayOf(charArrayOf('\u1FA9'), charArrayOf('\u1F69', '\u0399')),
                arrayOf(charArrayOf('\u1FAA'), charArrayOf('\u1F6A', '\u0399')),
                arrayOf(charArrayOf('\u1FAB'), charArrayOf('\u1F6B', '\u0399')),
                arrayOf(charArrayOf('\u1FAC'), charArrayOf('\u1F6C', '\u0399')),
                arrayOf(charArrayOf('\u1FAD'), charArrayOf('\u1F6D', '\u0399')),
                arrayOf(charArrayOf('\u1FAE'), charArrayOf('\u1F6E', '\u0399')),
                arrayOf(charArrayOf('\u1FAF'), charArrayOf('\u1F6F', '\u0399')),
                arrayOf(charArrayOf('\u1FB2'), charArrayOf('\u1FBA', '\u0399')),
                arrayOf(charArrayOf('\u1FB3'), charArrayOf('\u0391', '\u0399')),
                arrayOf(charArrayOf('\u1FB4'), charArrayOf('\u0386', '\u0399')),
                arrayOf(charArrayOf('\u1FB6'), charArrayOf('\u0391', '\u0342')),
                arrayOf(charArrayOf('\u1FB7'), charArrayOf('\u0391', '\u0342', '\u0399')),
                arrayOf(charArrayOf('\u1FBC'), charArrayOf('\u0391', '\u0399')),
                arrayOf(charArrayOf('\u1FC2'), charArrayOf('\u1FCA', '\u0399')),
                arrayOf(charArrayOf('\u1FC3'), charArrayOf('\u0397', '\u0399')),
                arrayOf(charArrayOf('\u1FC4'), charArrayOf('\u0389', '\u0399')),
                arrayOf(charArrayOf('\u1FC6'), charArrayOf('\u0397', '\u0342')),
                arrayOf(charArrayOf('\u1FC7'), charArrayOf('\u0397', '\u0342', '\u0399')),
                arrayOf(charArrayOf('\u1FCC'), charArrayOf('\u0397', '\u0399')),
                arrayOf(charArrayOf('\u1FD2'), charArrayOf('\u0399', '\u0308', '\u0300')),
                arrayOf(charArrayOf('\u1FD3'), charArrayOf('\u0399', '\u0308', '\u0301')),
                arrayOf(charArrayOf('\u1FD6'), charArrayOf('\u0399', '\u0342')),
                arrayOf(charArrayOf('\u1FD7'), charArrayOf('\u0399', '\u0308', '\u0342')),
                arrayOf(charArrayOf('\u1FE2'), charArrayOf('\u03A5', '\u0308', '\u0300')),
                arrayOf(charArrayOf('\u1FE3'), charArrayOf('\u03A5', '\u0308', '\u0301')),
                arrayOf(charArrayOf('\u1FE4'), charArrayOf('\u03A1', '\u0313')),
                arrayOf(charArrayOf('\u1FE6'), charArrayOf('\u03A5', '\u0342')),
                arrayOf(charArrayOf('\u1FE7'), charArrayOf('\u03A5', '\u0308', '\u0342')),
                arrayOf(charArrayOf('\u1FF2'), charArrayOf('\u1FFA', '\u0399')),
                arrayOf(charArrayOf('\u1FF3'), charArrayOf('\u03A9', '\u0399')),
                arrayOf(charArrayOf('\u1FF4'), charArrayOf('\u038F', '\u0399')),
                arrayOf(charArrayOf('\u1FF6'), charArrayOf('\u03A9', '\u0342')),
                arrayOf(charArrayOf('\u1FF7'), charArrayOf('\u03A9', '\u0342', '\u0399')),
                arrayOf(charArrayOf('\u1FFC'), charArrayOf('\u03A9', '\u0399')),
                arrayOf(charArrayOf('\uFB00'), charArrayOf('\u0046', '\u0046')),
                arrayOf(charArrayOf('\uFB01'), charArrayOf('\u0046', '\u0049')),
                arrayOf(charArrayOf('\uFB02'), charArrayOf('\u0046', '\u004C')),
                arrayOf(charArrayOf('\uFB03'), charArrayOf('\u0046', '\u0046', '\u0049')),
                arrayOf(charArrayOf('\uFB04'), charArrayOf('\u0046', '\u0046', '\u004C')),
                arrayOf(charArrayOf('\uFB05'), charArrayOf('\u0053', '\u0054')),
                arrayOf(charArrayOf('\uFB06'), charArrayOf('\u0053', '\u0054')),
                arrayOf(charArrayOf('\uFB13'), charArrayOf('\u0544', '\u0546')),
                arrayOf(charArrayOf('\uFB14'), charArrayOf('\u0544', '\u0535')),
                arrayOf(charArrayOf('\uFB15'), charArrayOf('\u0544', '\u053B')),
                arrayOf(charArrayOf('\uFB16'), charArrayOf('\u054E', '\u0546')),
                arrayOf(charArrayOf('\uFB17'), charArrayOf('\u0544', '\u053D'))
            )
            // THIS CODE WAS AUTOMATICALLY CREATED BY GenerateCharacter:
            val data = A_DATA.toCharArray()
            if(data.size != 930 * 2) {
                throw RuntimeException("error")
            }
            var i = 0
            var j = 0
            while (i < 930 * 2) {
                val entry: Int = data[i++].code shl 16
                A[j++] = entry or data[i++].code
            }
        }
    }
}
