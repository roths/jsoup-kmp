package kotlinx.io

/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */
open class IOException : Exception {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}