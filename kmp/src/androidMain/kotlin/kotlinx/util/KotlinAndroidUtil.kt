package kotlinx.util
/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */
actual fun identityHashCode(obj: Any?):Int = System.identityHashCode(obj)


actual class WeakReference<T : Any> {
    val inner:java.lang.ref.WeakReference<T>
    actual constructor(referred: T) {
        inner = java.lang.ref.WeakReference(referred)
    }
    /**
     * Returns either reference to an object or null, if it was collected.
     */
    actual val value: T?
        get() = inner.get()
}

actual class URL {

    private val url: java.net.URL

    actual constructor(base: URL, relUrl: String) {
        url = java.net.URL(base.url, relUrl)
    }

    actual constructor(urlStr: String) {
        url = java.net.URL(urlStr)
    }

    actual constructor(protocol: String, host: String, port: Int, file: String) {
        url = java.net.URL(protocol, host, port, file)
    }

    actual fun getProtocol(): String = url.protocol
    actual fun getHost(): String = url.host
    actual fun getPort(): Int = url.port
    actual fun getPath(): String = url.path
    actual fun getRef(): String? = url.ref
    actual fun getFile(): String = url.file
    actual override fun toString():String = url.toString()
}

actual open class ThreadLocal<T> {
    private val inner:java.lang.ThreadLocal<T>

    actual constructor() {
        inner = object : java.lang.ThreadLocal<T>() {
            override fun initialValue(): T? {
                return this@ThreadLocal.initialValue()
            }
        }
    }

    actual fun get():T?  = inner.get()

    actual fun set(value:T) {
        inner.set(value)
    }

    actual open fun initialValue(): T? = null
}