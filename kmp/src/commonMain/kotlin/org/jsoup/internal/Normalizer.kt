package org.jsoup.internal

/**
 * Util methods for normalizing strings. Jsoup internal use only, please don't depend on this API.
 */
object Normalizer {

    /** Lower-cases and trims the input string.  */
    fun normalize(input: String): String {
        return input.lowercase().trim { it <= ' ' }
    }

    /** If a string literal, just lower case the string; otherwise lower-case and trim.  */
    fun normalize(input: String, isStringLiteral: Boolean): String {
        return if (isStringLiteral) {
            input.lowercase()
        } else {
            normalize(input)
        }
    }
}