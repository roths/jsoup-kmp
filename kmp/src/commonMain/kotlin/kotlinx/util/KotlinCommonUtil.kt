package kotlinx.util
/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

class Stack<T> : MutableList<T> by mutableListOf() {
    /**
     * Pushes item to [Stack]
     * @param item Item to be pushed
     */
    fun push(item: T) = add(item)

    /**
     * Pops (removes and return) last item from [Stack]
     * @return item Last item if [Stack] is not empty, null otherwise
     */
    fun pop(): T = removeAt(lastIndex)

    /**
     * Peeks (return) last item from [Stack]
     * @return item Last item if [Stack] is not empty, null otherwise
     */
    fun peek(): T = this[lastIndex]
}

expect fun identityHashCode(obj: Any?):Int

fun Int.highestOneBit(): Int {
    // HD, Figure 3-1
    var i = this
    i = i or (i shr 1)
    i = i or (i shr 2)
    i = i or (i shr 4)
    i = i or (i shr 8)
    i = i or (i shr 16)
    return i - (i ushr 1)
}

expect class WeakReference<T : Any> {
    constructor(referred: T)
    val value: T?
}

expect class URL {

    constructor(urlStr:String)

    constructor(base: URL, relUrl: String)

    constructor(protocol: String, host: String, port: Int, file: String)

    fun getProtocol():String
    fun getHost():String
    fun getPort():Int
    fun getPath():String
    fun getRef():String?
    fun getFile():String
    override fun toString():String
}


expect open class ThreadLocal<T>{
    constructor()
    fun get(): T?
    fun set(value:T)

    open fun initialValue(): T?
}