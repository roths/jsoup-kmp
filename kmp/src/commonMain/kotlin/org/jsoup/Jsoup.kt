package org.jsoup

import org.jsoup.nodes.Document
import org.jsoup.nodes.Document.OutputSettings
import org.jsoup.parser.Parser
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist

/**
 * The core public access point to the jsoup functionality.
 *
 * @author Jonathan Hedley
 */
object Jsoup {

    /**
     * Parse HTML into a Document. The parser will make a sensible, balanced document tree out of any HTML.
     *
     * @param html    HTML to parse
     * @param baseUri The URL where the HTML was retrieved from. Used to resolve relative URLs to absolute URLs, that occur
     * before the HTML declares a `<base href>` tag.
     * @return sane HTML
     */
    fun parse(html: String, baseUri: String = "", parser: Parser? = null): Document {
        return if (parser != null) {
            parse(html, baseUri)
        } else {
            Parser.parse(html, baseUri)
        }
    }

    /**
     * Parse a fragment of HTML, with the assumption that it forms the `body` of the HTML.
     *
     * @param bodyHtml body HTML fragment
     * @param baseUri  URL to resolve relative URLs against.
     * @return sane HTML document
     * @see Document.body
     */
    fun parseBodyFragment(bodyHtml: String, baseUri: String): Document {
        return Parser.parseBodyFragment(bodyHtml, baseUri)
    }

    /**
     * Parse a fragment of HTML, with the assumption that it forms the `body` of the HTML.
     *
     * @param bodyHtml body HTML fragment
     * @return sane HTML document
     * @see Document.body
     */
    fun parseBodyFragment(bodyHtml: String): Document {
        return Parser.parseBodyFragment(bodyHtml, "")
    }

    /**
     * Get safe HTML from untrusted input HTML, by parsing input HTML and filtering it through an allow-list of safe
     * tags and attributes.
     *
     * @param bodyHtml  input untrusted HTML (body fragment)
     * @param baseUri   URL to resolve relative URLs against
     * @param safelist  list of permitted HTML elements
     * @return safe HTML (body fragment)
     * @see Cleaner.clean
     */
    fun clean(bodyHtml: String, baseUri: String, safelist: Safelist): String {
        val dirty: Document = parseBodyFragment(bodyHtml, baseUri)
        val cleaner = Cleaner(safelist)
        val clean: Document = cleaner.clean(dirty)
        return clean.body().html()
    }

    /**
     * Get safe HTML from untrusted input HTML, by parsing input HTML and filtering it through a safe-list of permitted
     * tags and attributes.
     *
     *
     * Note that as this method does not take a base href URL to resolve attributes with relative URLs against, those
     * URLs will be removed, unless the input HTML contains a `<base href> tag`. If you wish to preserve those, use
     * the [Jsoup.clean] method instead, and enable
     * [Safelist.preserveRelativeLinks].
     *
     * @param bodyHtml input untrusted HTML (body fragment)
     * @param safelist list of permitted HTML elements
     * @return safe HTML (body fragment)
     * @see Cleaner.clean
     */
    fun clean(bodyHtml: String, safelist: Safelist): String {
        return clean(bodyHtml, "", safelist)
    }

    /**
     * Get safe HTML from untrusted input HTML, by parsing input HTML and filtering it through a safe-list of
     * permitted tags and attributes.
     *
     * The HTML is treated as a body fragment; it's expected the cleaned HTML will be used within the body of an
     * existing document. If you want to clean full documents, use [Cleaner.clean] instead, and add
     * structural tags (`html, head, body` etc) to the safelist.
     *
     * @param bodyHtml input untrusted HTML (body fragment)
     * @param baseUri URL to resolve relative URLs against
     * @param safelist list of permitted HTML elements
     * @param outputSettings document output settings; use to control pretty-printing and entity escape modes
     * @return safe HTML (body fragment)
     * @see Cleaner.clean
     */
    fun clean(bodyHtml: String, baseUri: String, safelist: Safelist, outputSettings: OutputSettings): String {
        val dirty: Document = parseBodyFragment(bodyHtml, baseUri)
        val cleaner = Cleaner(safelist)
        val clean: Document = cleaner.clean(dirty)
        clean.outputSettings(outputSettings)
        return clean.body().html()
    }

    /**
     * Test if the input body HTML has only tags and attributes allowed by the Safelist. Useful for form validation.
     *
     * The input HTML should still be run through the cleaner to set up enforced attributes, and to tidy the output.
     *
     * Assumes the HTML is a body fragment (i.e. will be used in an existing HTML document body.)
     * @param bodyHtml HTML to test
     * @param safelist safelist to test against
     * @return true if no tags or attributes were removed; false otherwise
     * @see .clean
     */
    fun isValid(bodyHtml: String, safelist: Safelist): Boolean {
        return Cleaner(safelist).isValidBodyHtml(bodyHtml)
    }
}