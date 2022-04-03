package kotlinx

import kotlinx.lang.CharacterData

/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

/**
 * The minimum radix available for conversion to and from strings.
 * The constant value of this field is the smallest value permitted
 * for the radix argument in radix-conversion methods such as the
 * `digit` method, the `forDigit` method, and the
 * `toString` method of class `Integer`.
 *
 * @see Character.digit
 * @see Character.forDigit
 * @see Integer.toString
 * @see Integer.valueOf
 */
val Char.Companion.MIN_RADIX: Int
    get() = 2

/**
 * The maximum radix available for conversion to and from strings.
 * The constant value of this field is the largest value permitted
 * for the radix argument in radix-conversion methods such as the
 * `digit` method, the `forDigit` method, and the
 * `toString` method of class `Integer`.
 *
 * @see Character.digit
 * @see Character.forDigit
 * @see Integer.toString
 * @see Integer.valueOf
 */
val Char.Companion.MAX_RADIX: Int
    get() = 36

/**
 * The minimum value of a
 * <a href="http://www.unicode.org/glossary/#supplementary_code_point">
 * Unicode supplementary code point</a>, constant {@code U+10000}.
 *
 * @since 1.5
 */
val Char.Companion.MIN_SUPPLEMENTARY_CODE_POINT: Int
    get() = 0x010000

/**
 * The minimum value of a
 * <a href="http://www.unicode.org/glossary/#code_point">
 * Unicode code point</a>, constant {@code U+0000}.
 *
 * @since 1.5
 */
val Char.Companion.MAX_CODE_POINT: Int
    get() = 0X10FFFF

/*
     * Normative general types
     */
/*
     * General character types
     */
/**
 * General category "Cn" in the Unicode specification.
 * @since   1.1
 */
val Char.Companion.UNASSIGNED: Byte
    get() = 0

/**
 * General category "Co" in the Unicode specification.
 * @since   1.1
 */
val Char.Companion.PRIVATE_USE: Byte
    get() = 18

/**
 * General category "Lu" in the Unicode specification.
 * @since   1.1
 */
val Char.Companion.UPPERCASE_LETTER: Byte
    get() = 1

/**
 * General category "Ll" in the Unicode specification.
 * @since   1.1
 */
val Char.Companion.LOWERCASE_LETTER: Byte
    get() = 2

/**
 * General category "Lt" in the Unicode specification.
 * @since   1.1
 */
val Char.Companion.TITLECASE_LETTER: Byte
    get() = 3

/**
 * General category "Lm" in the Unicode specification.
 * @since   1.1
 */
val Char.Companion.MODIFIER_LETTER: Byte
    get() = 4

/**
 * General category "Lo" in the Unicode specification.
 * @since   1.1
 */
val Char.Companion.OTHER_LETTER: Byte
    get() = 5

/**
 * General category "Nd" in the Unicode specification.
 * @since   1.1
 */
val Char.Companion.DECIMAL_DIGIT_NUMBER: Byte
    get() = 9

/**
 * Error flag. Use int (code point) to avoid confusion with U+FFFF.
 */
val Char.Companion.ERROR: Int
    get() = -0x1

/**
 * Undefined bidirectional character type. Undefined `char`
 * values have undefined directionality in the Unicode specification.
 * @since 1.4
 */
val Char.Companion.DIRECTIONALITY_UNDEFINED: Byte
    get() = -1

/**
 * Strong bidirectional character type "L" in the Unicode specification.
 * @since 1.4
 */
val Char.Companion.DIRECTIONALITY_LEFT_TO_RIGHT: Byte
    get() = 0

/**
 * Strong bidirectional character type "LRE" in the Unicode specification.
 * @since 1.4
 */
val Char.Companion.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING: Byte
    get() = 14

/**
 * Strong bidirectional character type "LRO" in the Unicode specification.
 * @since 1.4
 */
val Char.Companion.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE: Byte
    get() = 15

/**
 * Strong bidirectional character type "RLE" in the Unicode specification.
 * @since 1.4
 */
val Char.Companion.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING: Byte
    get() = 16

/**
 * Strong bidirectional character type "RLO" in the Unicode specification.
 * @since 1.4
 */
val Char.Companion.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE: Byte
    get() = 17

/**
 * Weak bidirectional character type "PDF" in the Unicode specification.
 * @since 1.4
 */
val Char.Companion.DIRECTIONALITY_POP_DIRECTIONAL_FORMAT: Byte
    get() = 18

/**
 * Determines if the given {@code char} value is a
 * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">
 * Unicode high-surrogate code unit</a>
 * (also known as <i>leading-surrogate code unit</i>).
 *
 * <p>Such values do not represent characters by themselves,
 * but are used in the representation of
 * <a href="#supplementary">supplementary characters</a>
 * in the UTF-16 encoding.
 *
 * @param  ch the {@code char} value to be tested.
 * @return {@code true} if the {@code char} value is between
 *         {@link #MIN_HIGH_SURROGATE} and
 *         {@link #MAX_HIGH_SURROGATE} inclusive;
 *         {@code false} otherwise.
 * @see    Character#isLowSurrogate(char)
 * @see    Character.UnicodeBlock#of(int)
 * @since  1.5
 */
fun Char.Companion.isHighSurrogate(ch: Char): Boolean {
    // Help VM constant-fold; MAX_HIGH_SURROGATE + 1 == MIN_LOW_SURROGATE
    return ch >= MIN_HIGH_SURROGATE && ch < MAX_HIGH_SURROGATE + 1
}

/**
 * Determines if the given {@code char} value is a
 * <a href="http://www.unicode.org/glossary/#low_surrogate_code_unit">
 * Unicode low-surrogate code unit</a>
 * (also known as <i>trailing-surrogate code unit</i>).
 *
 * <p>Such values do not represent characters by themselves,
 * but are used in the representation of
 * <a href="#supplementary">supplementary characters</a>
 * in the UTF-16 encoding.
 *
 * @param  ch the {@code char} value to be tested.
 * @return {@code true} if the {@code char} value is between
 *         {@link #MIN_LOW_SURROGATE} and
 *         {@link #MAX_LOW_SURROGATE} inclusive;
 *         {@code false} otherwise.
 * @see    Character#isHighSurrogate(char)
 * @since  1.5
 */
fun Char.Companion.isLowSurrogate(ch: Char): Boolean {
    return ch >= MIN_LOW_SURROGATE && ch < MAX_LOW_SURROGATE + 1
}

/**
 * Returns the trailing surrogate (a
 * <a href="http://www.unicode.org/glossary/#low_surrogate_code_unit">
 * low surrogate code unit</a>) of the
 * <a href="http://www.unicode.org/glossary/#surrogate_pair">
 * surrogate pair</a>
 * representing the specified supplementary character (Unicode
 * code point) in the UTF-16 encoding.  If the specified character
 * is not a
 * <a href="Character.html#supplementary">supplementary character</a>,
 * an unspecified {@code char} is returned.
 *
 * <p>If
 * {@link #isSupplementaryCodePoint isSupplementaryCodePoint(x)}
 * is {@code true}, then
 * {@link #isLowSurrogate isLowSurrogate}{@code (lowSurrogate(x))} and
 * {@link #toCodePoint toCodePoint}{@code (}{@link #highSurrogate highSurrogate}{@code (x), lowSurrogate(x)) == x}
 * are also always {@code true}.
 *
 * @param   codePoint a supplementary character (Unicode code point)
 * @return  the trailing surrogate code unit used to represent the
 *          character in the UTF-16 encoding
 * @since   1.7
 */
fun Char.Companion.lowSurrogate(codePoint: Int): Char {
    return ((codePoint and 0x3ff) + MIN_LOW_SURROGATE.code).toChar()
}

/**
 * Returns the leading surrogate (a
 * <a href="http://www.unicode.org/glossary/#high_surrogate_code_unit">
 * high surrogate code unit</a>) of the
 * <a href="http://www.unicode.org/glossary/#surrogate_pair">
 * surrogate pair</a>
 * representing the specified supplementary character (Unicode
 * code point) in the UTF-16 encoding.  If the specified character
 * is not a
 * <a href="Character.html#supplementary">supplementary character</a>,
 * an unspecified {@code char} is returned.
 *
 * <p>If
 * {@link #isSupplementaryCodePoint isSupplementaryCodePoint(x)}
 * is {@code true}, then
 * {@link #isHighSurrogate isHighSurrogate}{@code (highSurrogate(x))} and
 * {@link #toCodePoint toCodePoint}{@code (highSurrogate(x), }{@link #lowSurrogate lowSurrogate}{@code (x)) == x}
 * are also always {@code true}.
 *
 * @param   codePoint a supplementary character (Unicode code point)
 * @return  the leading surrogate code unit used to represent the
 *          character in the UTF-16 encoding
 * @since   1.7
 */

fun Char.Companion.highSurrogate(codePoint: Int): Char {
    return ((codePoint ushr 10)
            + (MIN_HIGH_SURROGATE.code - (MIN_SUPPLEMENTARY_CODE_POINT ushr 10))).toChar()
}

/**
 * Converts the specified character (Unicode code point) to its
 * UTF-16 representation. If the specified code point is a BMP
 * (Basic Multilingual Plane or Plane 0) value, the same value is
 * stored in {@code dst[dstIndex]}, and 1 is returned. If the
 * specified code point is a supplementary character, its
 * surrogate values are stored in {@code dst[dstIndex]}
 * (high-surrogate) and {@code dst[dstIndex+1]}
 * (low-surrogate), and 2 is returned.
 *
 * @param  codePoint the character (Unicode code point) to be converted.
 * @param  dst an array of {@code char} in which the
 * {@code codePoint}'s UTF-16 value is stored.
 * @param dstIndex the start index into the {@code dst}
 * array where the converted value is stored.
 * @return 1 if the code point is a BMP code point, 2 if the
 * code point is a supplementary code point.
 * @exception IllegalArgumentException if the specified
 * {@code codePoint} is not a valid Unicode code point.
 * @exception NullPointerException if the specified {@code dst} is null.
 * @exception IndexOutOfBoundsException if {@code dstIndex}
 * is negative or not less than {@code dst.length}, or if
 * {@code dst} at {@code dstIndex} doesn't have enough
 * array element(s) to store the resulting {@code char}
 * value(s). (If {@code dstIndex} is equal to
 * {@code dst.length-1} and the specified
 * {@code codePoint} is a supplementary character, the
 * high-surrogate value is not stored in
 * {@code dst[dstIndex]}.)
 * @since  1.5
 */
fun Char.Companion.toSurrogates(codePoint: Int): CharArray {
    // We write elements "backwards" to guarantee all-or-nothing
    val dst = CharArray(2)
    dst[1] = lowSurrogate(codePoint)
    dst[0] = highSurrogate(codePoint)
    return dst
}

/**
 * Determines whether the specified character (Unicode code point)
 * is in the <a href="#BMP">Basic Multilingual Plane (BMP)</a>.
 * Such code points can be represented using a single {@code char}.
 *
 * @param  codePoint the character (Unicode code point) to be tested
 * @return {@code true} if the specified code point is between
 *         {@link #MIN_VALUE} and {@link #MAX_VALUE} inclusive;
 *         {@code false} otherwise.
 * @since  1.7
 */
fun Char.Companion.isBmpCodePoint(codePoint: Int): Boolean {
    return codePoint ushr 16 == 0
    // Optimized form of:
    //     codePoint >= MIN_VALUE && codePoint <= MAX_VALUE
    // We consistently use logical shift (>>>) to facilitate
    // additional runtime optimizations.
}

/**
 * Determines whether the specified code point is a valid
 * <a href="http://www.unicode.org/glossary/#code_point">
 * Unicode code point value</a>.
 *
 * @param  codePoint the Unicode code point to be tested
 * @return {@code true} if the specified code point value is between
 *         {@link #MIN_CODE_POINT} and
 *         {@link #MAX_CODE_POINT} inclusive;
 *         {@code false} otherwise.
 * @since  1.5
 */
fun Char.Companion.isValidCodePoint(codePoint: Int): Boolean {
    // Optimized form of:
    //     codePoint >= MIN_CODE_POINT && codePoint <= MAX_CODE_POINT
    val plane = codePoint ushr 16
    return plane < Char.MAX_CODE_POINT + 1 ushr 16
}

/**
 * Determines if the specified character is a letter or digit.
 * <p>
 * A character is considered to be a letter or digit if either
 * {@code Character.isLetter(char ch)} or
 * {@code Character.isDigit(char ch)} returns
 * {@code true} for the character.
 *
 * <p><b>Note:</b> This method cannot handle <a
 * href="#supplementary"> supplementary characters</a>. To support
 * all Unicode characters, including supplementary characters, use
 * the {@link #isLetterOrDigit(int)} method.
 *
 * @param   ch   the character to be tested.
 * @return  {@code true} if the character is a letter or digit;
 *          {@code false} otherwise.
 * @see     Character#isDigit(char)
 * @see     Character#isJavaIdentifierPart(char)
 * @see     Character#isJavaLetter(char)
 * @see     Character#isJavaLetterOrDigit(char)
 * @see     Character#isLetter(char)
 * @see     Character#isUnicodeIdentifierPart(char)
 * @since   1.0.2
 */

fun Char.Companion.isLetterOrDigit(ch: Char): Boolean {
    return isLetterOrDigit(ch.code)
}

/**
 * Determines if the specified character is white space according to Java.
 * A character is a Java whitespace character if and only if it satisfies
 * one of the following criteria:
 *
 *  *  It is a Unicode space character (`SPACE_SEPARATOR`,
 * `LINE_SEPARATOR`, or `PARAGRAPH_SEPARATOR`)
 * but is not also a non-breaking space (`'\u005Cu00A0'`,
 * `'\u005Cu2007'`, `'\u005Cu202F'`).
 *  *  It is `'\u005Ct'`, U+0009 HORIZONTAL TABULATION.
 *  *  It is `'\u005Cn'`, U+000A LINE FEED.
 *  *  It is `'\u005Cu000B'`, U+000B VERTICAL TABULATION.
 *  *  It is `'\u005Cf'`, U+000C FORM FEED.
 *  *  It is `'\u005Cr'`, U+000D CARRIAGE RETURN.
 *  *  It is `'\u005Cu001C'`, U+001C FILE SEPARATOR.
 *  *  It is `'\u005Cu001D'`, U+001D GROUP SEPARATOR.
 *  *  It is `'\u005Cu001E'`, U+001E RECORD SEPARATOR.
 *  *  It is `'\u005Cu001F'`, U+001F UNIT SEPARATOR.
 *
 *
 *
 * **Note:** This method cannot handle [ supplementary characters](#supplementary). To support
 * all Unicode characters, including supplementary characters, use
 * the [.isWhitespace] method.
 *
 * @param   ch the character to be tested.
 * @return  `true` if the character is a Java whitespace
 * character; `false` otherwise.
 * @see Character.isSpaceChar
 * @since   1.1
 */
fun Char.Companion.isWhitespace(ch: Char): Boolean {
    return isWhitespace(ch.code)
}

/**
 * Determines if the specified character (Unicode code point) is
 * white space according to Java.  A character is a Java
 * whitespace character if and only if it satisfies one of the
 * following criteria:
 *
 *  *  It is a Unicode space character ([.SPACE_SEPARATOR],
 * [.LINE_SEPARATOR], or [.PARAGRAPH_SEPARATOR])
 * but is not also a non-breaking space (`'\u005Cu00A0'`,
 * `'\u005Cu2007'`, `'\u005Cu202F'`).
 *  *  It is `'\u005Ct'`, U+0009 HORIZONTAL TABULATION.
 *  *  It is `'\u005Cn'`, U+000A LINE FEED.
 *  *  It is `'\u005Cu000B'`, U+000B VERTICAL TABULATION.
 *  *  It is `'\u005Cf'`, U+000C FORM FEED.
 *  *  It is `'\u005Cr'`, U+000D CARRIAGE RETURN.
 *  *  It is `'\u005Cu001C'`, U+001C FILE SEPARATOR.
 *  *  It is `'\u005Cu001D'`, U+001D GROUP SEPARATOR.
 *  *  It is `'\u005Cu001E'`, U+001E RECORD SEPARATOR.
 *  *  It is `'\u005Cu001F'`, U+001F UNIT SEPARATOR.
 *
 *
 *
 *
 * @param   codePoint the character (Unicode code point) to be tested.
 * @return  `true` if the character is a Java whitespace
 * character; `false` otherwise.
 * @see Character.isSpaceChar
 * @since   1.5
 */
fun Char.Companion.isWhitespace(codePoint: Int): Boolean {
    return CharacterData.of(codePoint).isWhitespace(codePoint)
}
/**
 * Determines if the specified character (Unicode code point) is a letter or digit.
 * <p>
 * A character is considered to be a letter or digit if either
 * {@link #isLetter(int) isLetter(codePoint)} or
 * {@link #isDigit(int) isDigit(codePoint)} returns
 * {@code true} for the character.
 *
 * @param   codePoint the character (Unicode code point) to be tested.
 * @return  {@code true} if the character is a letter or digit;
 *          {@code false} otherwise.
 * @see     Character#isDigit(int)
 * @see     Character#isJavaIdentifierPart(int)
 * @see     Character#isLetter(int)
 * @see     Character#isUnicodeIdentifierPart(int)
 * @since   1.5
 */
fun Char.Companion.isLetterOrDigit(codePoint: Int): Boolean {
    return (1 shl Char.UPPERCASE_LETTER.toInt() or
            (1 shl Char.LOWERCASE_LETTER.toInt()) or
            (1 shl Char.TITLECASE_LETTER.toInt()) or
            (1 shl Char.MODIFIER_LETTER.toInt()) or
            (1 shl Char.OTHER_LETTER.toInt()) or
            (1 shl Char.DECIMAL_DIGIT_NUMBER.toInt()) shr Char.getType(codePoint) and 1
            != 0)
}

/**
 * Returns a value indicating a character's general category.
 *
 * @param   codePoint the character (Unicode code point) to be tested.
 * @return  a value of type `int` representing the
 * character's general category.
 * @see Character.COMBINING_SPACING_MARK COMBINING_SPACING_MARK
 * @see Character.CONNECTOR_PUNCTUATION CONNECTOR_PUNCTUATION
 * @see Character.CONTROL CONTROL
 * @see Character.CURRENCY_SYMBOL CURRENCY_SYMBOL
 * @see Character.DASH_PUNCTUATION DASH_PUNCTUATION
 * @see Character.DECIMAL_DIGIT_NUMBER DECIMAL_DIGIT_NUMBER
 * @see Character.ENCLOSING_MARK ENCLOSING_MARK
 * @see Character.END_PUNCTUATION END_PUNCTUATION
 * @see Character.FINAL_QUOTE_PUNCTUATION FINAL_QUOTE_PUNCTUATION
 * @see Character.FORMAT FORMAT
 * @see Character.INITIAL_QUOTE_PUNCTUATION INITIAL_QUOTE_PUNCTUATION
 * @see Character.LETTER_NUMBER LETTER_NUMBER
 * @see Character.LINE_SEPARATOR LINE_SEPARATOR
 * @see Character.LOWERCASE_LETTER LOWERCASE_LETTER
 * @see Character.MATH_SYMBOL MATH_SYMBOL
 * @see Character.MODIFIER_LETTER MODIFIER_LETTER
 * @see Character.MODIFIER_SYMBOL MODIFIER_SYMBOL
 * @see Character.NON_SPACING_MARK NON_SPACING_MARK
 * @see Character.OTHER_LETTER OTHER_LETTER
 * @see Character.OTHER_NUMBER OTHER_NUMBER
 * @see Character.OTHER_PUNCTUATION OTHER_PUNCTUATION
 * @see Character.OTHER_SYMBOL OTHER_SYMBOL
 * @see Character.PARAGRAPH_SEPARATOR PARAGRAPH_SEPARATOR
 * @see Character.PRIVATE_USE PRIVATE_USE
 * @see Character.SPACE_SEPARATOR SPACE_SEPARATOR
 * @see Character.START_PUNCTUATION START_PUNCTUATION
 * @see Character.SURROGATE SURROGATE
 * @see Character.TITLECASE_LETTER TITLECASE_LETTER
 * @see Character.UNASSIGNED UNASSIGNED
 * @see Character.UPPERCASE_LETTER UPPERCASE_LETTER
 * @since   1.5
 */
fun Char.Companion.getType(codePoint: Int): Int {
    return CharacterData.of(codePoint).getType(codePoint)
}

/**
 * Determines if the specified character is a letter.
 *
 *
 * A character is considered to be a letter if its general
 * category type, provided by `Character.getType(ch)`,
 * is any of the following:
 *
 *  *  `UPPERCASE_LETTER`
 *  *  `LOWERCASE_LETTER`
 *  *  `TITLECASE_LETTER`
 *  *  `MODIFIER_LETTER`
 *  *  `OTHER_LETTER`
 *
 *
 * Not all letters have case. Many characters are
 * letters but are neither uppercase nor lowercase nor titlecase.
 *
 *
 * **Note:** This method cannot handle [ supplementary characters](#supplementary). To support
 * all Unicode characters, including supplementary characters, use
 * the [.isLetter] method.
 *
 * @param   ch   the character to be tested.
 * @return  `true` if the character is a letter;
 * `false` otherwise.
 * @see Character.isDigit
 * @see Character.isJavaIdentifierStart
 * @see Character.isJavaLetter
 * @see Character.isJavaLetterOrDigit
 * @see Character.isLetterOrDigit
 * @see Character.isLowerCase
 * @see Character.isTitleCase
 * @see Character.isUnicodeIdentifierStart
 * @see Character.isUpperCase
 */
fun Char.Companion.isLetter(ch: Char): Boolean {
    return isLetter(ch.code)
}

/**
 * Determines if the specified character (Unicode code point) is a letter.
 *
 *
 * A character is considered to be a letter if its general
 * category type, provided by [getType(codePoint)][Character.getType],
 * is any of the following:
 *
 *  *  `UPPERCASE_LETTER`
 *  *  `LOWERCASE_LETTER`
 *  *  `TITLECASE_LETTER`
 *  *  `MODIFIER_LETTER`
 *  *  `OTHER_LETTER`
 *
 *
 * Not all letters have case. Many characters are
 * letters but are neither uppercase nor lowercase nor titlecase.
 *
 * @param   codePoint the character (Unicode code point) to be tested.
 * @return  `true` if the character is a letter;
 * `false` otherwise.
 * @see Character.isDigit
 * @see Character.isJavaIdentifierStart
 * @see Character.isLetterOrDigit
 * @see Character.isLowerCase
 * @see Character.isTitleCase
 * @see Character.isUnicodeIdentifierStart
 * @see Character.isUpperCase
 * @since   1.5
 */
fun Char.Companion.isLetter(codePoint: Int): Boolean {
    return (1 shl Char.UPPERCASE_LETTER.toInt() or
            (1 shl Char.LOWERCASE_LETTER.toInt()) or
            (1 shl Char.TITLECASE_LETTER.toInt()) or
            (1 shl Char.MODIFIER_LETTER.toInt()) or
            (1 shl Char.OTHER_LETTER.toInt()) shr Char.getType(codePoint) and 1
            != 0)
}

/**
 * Converts the character argument to uppercase using case mapping
 * information from the UnicodeData file.
 *
 *
 * Note that
 * `Character.isUpperCase(Character.toUpperCase(ch))`
 * does not always return `true` for some ranges of
 * characters, particularly those that are symbols or ideographs.
 *
 *
 * In general, [String.toUpperCase] should be used to map
 * characters to uppercase. `String` case mapping methods
 * have several benefits over `Character` case mapping methods.
 * `String` case mapping methods can perform locale-sensitive
 * mappings, context-sensitive mappings, and 1:M character mappings, whereas
 * the `Character` case mapping methods cannot.
 *
 *
 * **Note:** This method cannot handle [ supplementary characters](#supplementary). To support
 * all Unicode characters, including supplementary characters, use
 * the [.toUpperCase] method.
 *
 * @param   ch   the character to be converted.
 * @return  the uppercase equivalent of the character, if any;
 * otherwise, the character itself.
 * @see Character.isUpperCase
 * @see String.toUpperCase
 */
fun Char.Companion.toUpperCase(ch: Char): Char {
    return toUpperCase(ch.code).toChar()
}

/**
 * Converts the character (Unicode code point) argument to
 * uppercase using case mapping information from the UnicodeData
 * file.
 *
 *
 * Note that
 * `Character.isUpperCase(Character.toUpperCase(codePoint))`
 * does not always return `true` for some ranges of
 * characters, particularly those that are symbols or ideographs.
 *
 *
 * In general, [String.toUpperCase] should be used to map
 * characters to uppercase. `String` case mapping methods
 * have several benefits over `Character` case mapping methods.
 * `String` case mapping methods can perform locale-sensitive
 * mappings, context-sensitive mappings, and 1:M character mappings, whereas
 * the `Character` case mapping methods cannot.
 *
 * @param   codePoint   the character (Unicode code point) to be converted.
 * @return  the uppercase equivalent of the character, if any;
 * otherwise, the character itself.
 * @see Character.isUpperCase
 * @see String.toUpperCase
 * @since   1.5
 */
fun Char.Companion.toUpperCase(codePoint: Int): Int {
    return CharacterData.of(codePoint).toUpperCase(codePoint)
}

fun Char.Companion.charCount(codePoint: Int): Int {
    return if (codePoint >= Char.MIN_SUPPLEMENTARY_CODE_POINT) 2 else 1
}

/**
 * Converts the specified character (Unicode code point) to its
 * UTF-16 representation stored in a `char` array. If
 * the specified code point is a BMP (Basic Multilingual Plane or
 * Plane 0) value, the resulting `char` array has
 * the same value as `codePoint`. If the specified code
 * point is a supplementary code point, the resulting
 * `char` array has the corresponding surrogate pair.
 *
 * @param  codePoint a Unicode code point
 * @return a `char` array having
 * `codePoint`'s UTF-16 representation.
 * @exception IllegalArgumentException if the specified
 * `codePoint` is not a valid Unicode code point.
 * @since  1.5
 */
fun Char.Companion.toChars(codePoint: Int): CharArray {
    return if (Char.isBmpCodePoint(codePoint)) {
        charArrayOf(codePoint.toChar())
    } else if (Char.isValidCodePoint(codePoint)) {
        Char.toSurrogates(codePoint)
    } else {
        throw IllegalArgumentException()
    }
}