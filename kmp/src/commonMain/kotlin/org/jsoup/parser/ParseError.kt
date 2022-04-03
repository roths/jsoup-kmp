package org.jsoup.parser

/**
 * A Parse Error records an error in the input HTML that occurs in either the tokenisation or the tree building phase.
 */
class ParseError {

    private var pos: Int
    private var cursorPos: String
    private var errorMsg: String

    internal constructor(reader: CharacterReader, errorMsg: String) {
        pos = reader.pos()
        cursorPos = reader.cursorPos()
        this.errorMsg = errorMsg
    }

    internal constructor(reader: CharacterReader?, errorFormat: String?, vararg args: Any?) {
        pos = reader!!.pos()
        cursorPos = reader!!.cursorPos()
        errorMsg = "$errorFormat,$args"
    }

    internal constructor(pos: Int, errorMsg: String) {
        this.pos = pos
        cursorPos = pos.toString()
        this.errorMsg = errorMsg
    }

    internal constructor(pos: Int, errorFormat: String?, vararg args: Any?) {
        this.pos = pos
        cursorPos = pos.toString()
        errorMsg = "$errorFormat,$args"
    }

    /**
     * Retrieve the error message.
     * @return the error message.
     */
    fun getErrorMessage(): String {
        return errorMsg
    }

    /**
     * Retrieves the offset of the error.
     * @return error offset within input
     */
    fun getPosition(): Int {
        return pos
    }

    /**
     * Get the formatted line:column cursor position where the error occured.
     * @return line:number cursor position
     */
    fun getCursorPos(): String {
        return cursorPos
    }

    override fun toString(): String {
        return "<$cursorPos>: $errorMsg"
    }
}