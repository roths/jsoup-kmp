package kotlinx.lang
import kotlinx.*

/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

/** The CharacterData class encapsulates the large tables once found in
 * java.lang.Character.
 */
internal class CharacterData01 private constructor() : CharacterData() {

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
        return A[Y[X[offset.code shr 5].code shl 4 or (offset.code shr 1 and 0xF)].code shl 1 or (offset.toInt() and 0x1)]
    }

    fun getPropertiesEx(ch: Int): Int {
        val offset = ch.toChar()
        return B[Y[X[offset.code shr 5].code shl 4 or (offset.code shr 1 and 0xF)].code shl 1 or (offset.toInt() and 0x1)]
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
            val offset = `val` shl 5 shr 5 + 18
            mapChar = ch + offset
        }
        return mapChar
    }

    override fun toUpperCase(ch: Int): Int {
        var mapChar = ch
        val `val` = getProperties(ch)
        if (`val` and 0x00010000 != 0) {
            val offset = `val` shl 5 shr 5 + 18
            mapChar = ch - offset
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
                0x10113 -> 40
                0x10114 -> 50
                0x10115 -> 60
                0x10116 -> 70
                0x10117 -> 80
                0x10118 -> 90
                0x10119 -> 100
                0x1011A -> 200
                0x1011B -> 300
                0x1011C -> 400
                0x1011D -> 500
                0x1011E -> 600
                0x1011F -> 700
                0x10120 -> 800
                0x10121 -> 900
                0x10122 -> 1000
                0x10123 -> 2000
                0x10124 -> 3000
                0x10125 -> 4000
                0x10126 -> 5000
                0x10127 -> 6000
                0x10128 -> 7000
                0x10129 -> 8000
                0x1012A -> 9000
                0x1012B -> 10000
                0x1012C -> 20000
                0x1012D -> 30000
                0x1012E -> 40000
                0x1012F -> 50000
                0x10130 -> 60000
                0x10131 -> 70000
                0x10132 -> 80000
                0x10133 -> 90000
                0x10323 -> 50
                0x010144 -> 50
                0x010145 -> 500
                0x010146 -> 5000
                0x010147 -> 50000
                0x01014A -> 50
                0x01014B -> 100
                0x01014C -> 500
                0x01014D -> 1000
                0x01014E -> 5000
                0x010151 -> 50
                0x010152 -> 100
                0x010153 -> 500
                0x010154 -> 1000
                0x010155 -> 10000
                0x010156 -> 50000
                0x010166 -> 50
                0x010167 -> 50
                0x010168 -> 50
                0x010169 -> 50
                0x01016A -> 100
                0x01016B -> 300
                0x01016C -> 500
                0x01016D -> 500
                0x01016E -> 500
                0x01016F -> 500
                0x010170 -> 500
                0x010171 -> 1000
                0x010172 -> 5000
                0x010174 -> 50
                0x010341 -> 90
                0x01034A -> 900
                0x0103D5 -> 100
                0x01085D -> 100
                0x01085E -> 1000
                0x01085F -> 10000
                0x010919 -> 100
                0x010A46 -> 100
                0x010A47 -> 1000
                0x010A7E -> 50
                0x010B5E -> 100
                0x010B5F -> 1000
                0x010B7E -> 100
                0x010B7F -> 1000
                0x010E6C -> 40
                0x010E6D -> 50
                0x010E6E -> 60
                0x010E6F -> 70
                0x010E70 -> 80
                0x010E71 -> 90
                0x010E72 -> 100
                0x010E73 -> 200
                0x010E74 -> 300
                0x010E75 -> 400
                0x010E76 -> 500
                0x010E77 -> 600
                0x010E78 -> 700
                0x010E79 -> 800
                0x010E7A -> 900
                0x01105E -> 40
                0x01105F -> 50
                0x011060 -> 60
                0x011061 -> 70
                0x011062 -> 80
                0x011063 -> 90
                0x011064 -> 100
                0x011065 -> 1000
                0x012432 -> 216000
                0x012433 -> 432000
                0x01D36C -> 40
                0x01D36D -> 50
                0x01D36E -> 60
                0x01D36F -> 70
                0x01D370 -> 80
                0x01D371 -> 90
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
            directionality = Char.DIRECTIONALITY_UNDEFINED
        }
        return directionality
    }

    override fun isMirrored(ch: Int): Boolean {
        val props = getProperties(ch)
        return props and -0x80000000 != 0
    }

    companion object {

        val instance = CharacterData01()

        // The following tables and code generated using:
        // java GenerateCharacter -plane 1 -template /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/characterdata/CharacterData01.java.template -spec /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/unicodedata/UnicodeData.txt -specialcasing /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/unicodedata/SpecialCasing.txt -proplist /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/jdk/make/data/unicodedata/PropList.txt -o /Users/java_re/workspace/8-2-build-macosx-x86_64/jdk8u151/9699/build/macosx-x86_64/jdk/gensrc/java/lang/CharacterData01.java -string -usecharforbyte 11 4 1
        // The X table has 2048 entries for a total of 4096 bytes.
        val X = """ 	

 !"#$%&'()*+,-./0123456789:;<8=>?@ABBBBBBBCBDBEFGHIIJIIKLMNOPQRSTUVWXYMNZP[\]T^_`abcdefghijklmnoIpIIqrstuBvwxywzI{I|qI}~IIIIIIIIIIIIIq""".toCharArray()

        // The Y table has 2176 entries for a total of 4352 bytes.
        val Y =
            """                                                                       	




























                                      !"        #    $               %      &'())))))))))))))))))))********************                       +++++,,,-,,,,,,,,,,,,,,,,,,,,,,.--.,,,,,,,,,,,/0123,,,,,,,,,,,4567,,,,,,,,,,,,,/,,,,,,,,,,,,,,,,,,,,,,,,,,,,,89:;99,,.,.,,,,,,,,,,,,,<=>??@3AAAAB,,,,,,,,,,,,,,CD,,,,,,,,,,,7EEE,,,,,,,,,,,00F3,,,,,,,,,-00F3,,,,-GGGGGHIIIIIIIIIJKL                          9999999MNNNOOOOOPQQQQQ<L                      RK9STMUNN            VVVVV9W                 X99K99Y=ZZZZZNN9L                       [R9999S\ ]NVVVVV     XKR999^       ____```abbcddddeefghhhbijklmdnopqrstuvvwxyzk{kkkk$NN              [RRRRRRRRRRRRRRRRRRRRRR|><}~~~~~~ ^<<<< <<<<<<


""".toCharArray()

        // The A table has 320 entries for a total of 1280 bytes.
        val A = IntArray(320)
        const val A_DATA =
            "\u0000\u7005\u0000\u7005\u7800\u0000\u0000\u7005\u0000\u7005\u7800\u0000\u7800\u0000\u7800" +
                    "\u0000\u0000\u0018\u6800\u0018\u0000\u0018\u7800\u0000\u7800\u0000\u0000\u074B\u0000\u074B\u0000" +
                    "\u074B\u0000\u074B\u0000\u046B\u0000\u058B\u0000\u080B\u0000\u080B\u0000\u080B\u7800\u0000" +
                    "\u0000\u001c\u0000\u001c\u0000\u001c\u6800\u780A\u6800\u780A\u6800\u77EA\u6800\u744A\u6800" +
                    "\u77AA\u6800\u742A\u6800\u780A\u6800\u76CA\u6800\u774A\u6800\u780A\u6800\u780A" +
                    "\u6800\u766A\u6800\u752A\u6800\u750A\u6800\u74EA\u6800\u74EA\u6800\u74CA\u6800" +
                    "\u74AA\u6800\u748A\u6800\u74CA\u6800\u754A\u6800\u752A\u6800\u750A\u6800\u74EA" +
                    "\u6800\u74CA\u6800\u772A\u6800\u780A\u6800\u764A\u6800\u780A\u6800\u080B\u6800" +
                    "\u080B\u6800\u080B\u6800\u080B\u6800\u001c\u6800\u001c\u6800\u001c\u6800\u06CB\u7800" +
                    "\u0000\u0000\u001c\u4000\u3006\u0000\u042B\u0000\u048B\u0000\u050B\u0000\u080B\u0000\u7005" +
                    "\u0000\u780A\u0000\u780A\u7800\u0000\u7800\u0000\u0000\u0018\u0000\u0018\u0000\u760A\u0000\u760A" +
                    "\u0000\u76EA\u0000\u740A\u0000\u780A\u00a2\u7001\u00a2\u7001\u00a1\u7002\u00a1\u7002\u0000" +
                    "\u3409\u0000\u3409\u0800\u7005\u0800\u7005\u0800\u7005\u7800\u0000\u7800\u0000\u0800" +
                    "\u7005\u7800\u0000\u0800\u0018\u0800\u052B\u0800\u052B\u0800\u052B\u0800\u05EB" +
                    "\u0800\u070B\u0800\u080B\u0800\u080B\u0800\u080B\u0800\u056B\u0800\u066B\u0800" +
                    "\u078B\u0800\u080B\u0800\u050B\u0800\u050B\u7800\u0000\u6800\u0018\u0800\u7005" +
                    "\u4000\u3006\u4000\u3006\u4000\u3006\u7800\u0000\u4000\u3006\u4000\u3006\u7800" +
                    "\u0000\u4000\u3006\u4000\u3006\u4000\u3006\u7800\u0000\u7800\u0000\u4000\u3006\u0800" +
                    "\u042B\u0800\u042B\u0800\u04CB\u0800\u05EB\u0800\u0018\u0800\u0018\u0800\u0018\u7800" +
                    "\u0000\u0800\u7005\u0800\u048B\u0800\u080B\u0800\u0018\u6800\u0018\u6800\u0018\u0800" +
                    "\u05CB\u0800\u06EB\u3000\u042B\u3000\u042B\u3000\u054B\u3000\u066B\u3000\u080B" +
                    "\u3000\u080B\u3000\u080B\u7800\u0000\u0000\u3008\u4000\u3006\u0000\u3008\u0000\u7005" +
                    "\u4000\u3006\u0000\u0018\u0000\u0018\u0000\u0018\u6800\u05EB\u6800\u05EB\u6800\u070B\u6800" +
                    "\u042B\u0000\u3749\u0000\u3749\u0000\u3008\u0000\u3008\u4000\u3006\u0000\u3008\u0000\u3008" +
                    "\u4000\u3006\u0000\u0018\u0000\u1010\u0000\u3609\u0000\u3609\u4000\u3006\u0000\u7005\u0000" +
                    "\u7005\u4000\u3006\u4000\u3006\u4000\u3006\u0000\u3549\u0000\u3549\u0000\u7005\u0000" +
                    "\u3008\u0000\u3008\u0000\u7005\u0000\u7005\u0000\u0018\u0000\u3008\u4000\u3006\u0000\u744A" +
                    "\u0000\u744A\u0000\u776A\u0000\u776A\u0000\u776A\u0000\u76AA\u0000\u76AA\u0000\u76AA\u0000" +
                    "\u76AA\u0000\u758A\u0000\u758A\u0000\u758A\u0000\u746A\u0000\u746A\u0000\u746A\u0000\u77EA" +
                    "\u0000\u77EA\u0000\u77CA\u0000\u77CA\u0000\u77CA\u0000\u76AA\u0000\u768A\u0000\u768A\u0000" +
                    "\u768A\u0000\u780A\u0000\u780A\u0000\u75AA\u0000\u75AA\u0000\u75AA\u0000\u758A\u0000\u752A" +
                    "\u0000\u750A\u0000\u750A\u0000\u74EA\u0000\u74CA\u0000\u74AA\u0000\u74CA\u0000\u74CA\u0000" +
                    "\u74AA\u0000\u748A\u0000\u748A\u0000\u746A\u0000\u746A\u0000\u744A\u0000\u742A\u0000\u740A" +
                    "\u0000\u770A\u0000\u770A\u0000\u770A\u0000\u764A\u0000\u764A\u0000\u764A\u0000\u764A\u0000" +
                    "\u762A\u0000\u762A\u0000\u760A\u0000\u752A\u0000\u752A\u0000\u3008\u7800\u0000\u4000\u3006" +
                    "\u0000\u7004\u0000\u7004\u0000\u7004\u0000\u001c\u7800\u0000\u0000\u001c\u0000\u3008\u0000\u3008" +
                    "\u0000\u3008\u0000\u3008\u4800\u1010\u4800\u1010\u4800\u1010\u4800\u1010\u4000" +
                    "\u3006\u4000\u3006\u0000\u001c\u4000\u3006\u6800\u001c\u6800\u001c\u7800\u0000\u0000\u042B" +
                    "\u0000\u042B\u0000\u054B\u0000\u066B\u0000\u7001\u0000\u7001\u0000\u7002\u0000\u7002\u0000" +
                    "\u7002\u7800\u0000\u0000\u7001\u7800\u0000\u7800\u0000\u0000\u7001\u7800\u0000\u0000\u7002" +
                    "\u0000\u7001\u0000\u0019\u0000\u7002\uE800\u0019\u0000\u7001\u0000\u7002\u1800\u3649\u1800" +
                    "\u3649\u1800\u3509\u1800\u3509\u1800\u37C9\u1800\u37C9\u1800\u3689\u1800\u3689" +
                    "\u1800\u3549\u1800\u3549\u1000\u7005\u1000\u7005\u7800\u0000\u1000\u7005\u1000" +
                    "\u7005\u7800\u0000\u6800\u0019\u6800\u0019\u7800\u0000\u6800\u001c\u1800\u040B\u1800" +
                    "\u07EB\u1800\u07EB\u1800\u07EB\u1800\u07EB\u7800\u0000"

        // The B table has 320 entries for a total of 640 bytes.
        val B =
            ("\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0004" +
                    "\u0004\u0004\u0000\u0004\u0004\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0004\u0004" +
                    "\u0004\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0004\u0004\u0004\u0004\u0004\u0000\u0000" +
                    "\u0000\u0000\u0000\u0004\u0000\u0000\u0004\u0004\u0000\u0000\u0000\u0000\u0004\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0004\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
                    "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000").toCharArray()

        // In all, the character property tables require 9728 bytes.
        init {
            // THIS CODE WAS AUTOMATICALLY CREATED BY GenerateCharacter:
            val data = A_DATA.toCharArray()
            if(data.size != 320 * 2) {
                throw RuntimeException("error")
            }
            var i = 0
            var j = 0
            while (i < 320 * 2) {
                val entry: Int = data[i++].code shl 16
                A[j++] = entry or data[i++].code
            }
        }
    }
}
