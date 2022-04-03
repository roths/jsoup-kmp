package kotlinx


/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

private fun toCodePoint(high: Char, low: Char): Int {
    // Optimized form of:
    // return ((high - MIN_HIGH_SURROGATE) << 10)
    //         + (low - MIN_LOW_SURROGATE)
    //         + MIN_SUPPLEMENTARY_CODE_POINT;
    return (high.code shl 10) + low.code + (Char.MIN_SUPPLEMENTARY_CODE_POINT
            - (Char.MIN_HIGH_SURROGATE.code shl 10)
            - Char.MIN_LOW_SURROGATE.code)
}

private fun codePointAtImpl(a: CharArray, index: Int, limit: Int): Int {
    var index = index
    val c1 = a[index]
    if (Char.isHighSurrogate(c1) && ++index < limit) {
        val c2 = a[index]
        if (Char.isLowSurrogate(c2)) {
            return toCodePoint(c1, c2)
        }
    }
    return c1.code
}

fun String.codePoint(index: Int): Int {
    if (index < 0 || index >= length) {
        throw RuntimeException("String index out of range, index:$index, length:$length")
    }
    val value = toCharArray()
    return codePointAtImpl(value, index, value.size)
}

fun StringBuilder.appendCodePoint(codePoint: Int): StringBuilder {
    if (Char.isBmpCodePoint(codePoint)) {
        append(codePoint.toChar())
    } else if (Char.isValidCodePoint(codePoint)) {
        append(Char.toSurrogates(codePoint))
    } else {
        throw IllegalArgumentException()
    }
    return this
}