package org.jsoup.parser

import org.jsoup.nodes.Attributes

/**
 * Controls parser settings, to optionally preserve tag and/or attribute name case.
 */
class ParseSettings
/**
 * Define parse settings.
 * @param tag preserve tag case?
 * @param attribute preserve attribute name case?
 */(private val preserveTagCase: Boolean, private val preserveAttributeCase: Boolean) {

    companion object {

        /**
         * HTML default settings: both tag and attribute names are lower-cased during parsing.
         */
        val htmlDefault: ParseSettings

        /**
         * Preserve both tag and attribute case.
         */
        val preserveCase: ParseSettings

        /** Returns the normal name that a Tag will have (trimmed and lower-cased)  */
        fun normalName(name: String): String {
            return name.trim { it <= ' ' }.lowercase()
        }

        init {
            htmlDefault = ParseSettings(false, false)
            preserveCase = ParseSettings(true, true)
        }
    }

    /**
     * Returns true if preserving tag name case.
     */
    fun preserveTagCase(): Boolean {
        return preserveTagCase
    }

    /**
     * Returns true if preserving attribute case.
     */
    fun preserveAttributeCase(): Boolean {
        return preserveAttributeCase
    }

    internal constructor(copy: ParseSettings) : this(copy.preserveTagCase, copy.preserveAttributeCase) {}

    /**
     * Normalizes a tag name according to the case preservation setting.
     */
    fun normalizeTag(name: String): String {
        var name = name
        name = name.trim { it <= ' ' }
        if (!preserveTagCase) name = name.lowercase()
        return name
    }

    /**
     * Normalizes an attribute according to the case preservation setting.
     */
    fun normalizeAttribute(name: String): String {
        var name = name
        name = name.trim { it <= ' ' }
        if (!preserveAttributeCase) name = name.lowercase()
        return name
    }

    fun normalizeAttributes(attributes: Attributes?): Attributes? {
        if (attributes != null && !preserveAttributeCase) {
            attributes.normalize()
        }
        return attributes
    }
}