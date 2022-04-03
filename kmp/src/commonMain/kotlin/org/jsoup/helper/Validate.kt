package org.jsoup.helper

/**
 * Simple validation methods. Designed for jsoup internal use
 */
object Validate {

    /**
     * Validates that the object is not null
     * @param obj object to test
     */
    fun notNull(obj: Any?) {
        requireNotNull(obj) { "Object must not be null" }
    }

    /**
     * Validates that the object is not null
     * @param obj object to test
     * @param msg message to output if validation fails
     */
    fun notNull(obj: Any?, msg: String) {
        requireNotNull(obj) { msg }
    }

    /**
     * Validates that the value is true
     * @param val object to test
     */
    fun isTrue(`val`: Boolean) {
        require(`val`) { "Must be true" }
    }

    /**
     * Validates that the value is true
     * @param val object to test
     * @param msg message to output if validation fails
     */
    fun isTrue(`val`: Boolean, msg: String) {
        require(`val`) { msg }
    }

    /**
     * Validates that the value is false
     * @param val object to test
     */
    fun isFalse(`val`: Boolean) {
        require(!`val`) { "Must be false" }
    }

    /**
     * Validates that the value is false
     * @param val object to test
     * @param msg message to output if validation fails
     */
    fun isFalse(`val`: Boolean, msg: String) {
        require(!`val`) { msg }
    }
    /**
     * Validates that the array contains no null elements
     * @param objects the array to test
     * @param msg message to output if validation fails
     */
    /**
     * Validates that the array contains no null elements
     * @param objects the array to test
     */
    fun noNullElements(objects: Array<Any?>, msg: String = "Array must not contain any null objects") {
        for (obj in objects) requireNotNull(obj) { msg }
    }

    /**
     * Validates that the string is not null and is not empty
     * @param string the string to test
     */
    fun notEmpty(string: String?) {
        require(!(string == null || string.length == 0)) { "String must not be empty" }
    }

    /**
     * Validates that the string is not null and is not empty
     * @param string the string to test
     * @param msg message to output if validation fails
     */
    fun notEmpty(string: String?, msg: String) {
        require(!(string == null || string.length == 0)) { msg }
    }

    /**
     * Blow up if we reach an unexpected state.
     * @param msg message to think about
     */
    fun wtf(msg: String?) {
        throw IllegalStateException(msg)
    }

    /**
     * Cause a failure.
     * @param msg message to output.
     */
    fun fail(msg: String?) {
        throw IllegalArgumentException(msg)
    }
}