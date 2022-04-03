package org.jsoup.helper

import io.ktor.utils.io.charsets.Charset

/**
 * Internal static utilities for handling data.
 *
 */
object DataUtil {
    val UTF_8 = Charset.forName("UTF-8") // Don't use StandardCharsets, as those only appear in Android API 19, and we target 10.
}