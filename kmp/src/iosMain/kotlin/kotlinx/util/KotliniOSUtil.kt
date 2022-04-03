package kotlinx.util
import platform.Foundation.NSURL

import kotlin.native.identityHashCode
/**
 *
 * created by luoqiaoyou on 2022/4/3.
 */

actual fun identityHashCode(obj: Any?):Int = obj.identityHashCode()

actual class WeakReference<T : Any> {
    val inner:kotlin.native.ref.WeakReference<T>
    actual constructor(referred: T) {
        inner = kotlin.native.ref.WeakReference(referred)
    }
    /**
     * Returns either reference to an object or null, if it was collected.
     */
    actual val value: T?
        get() = inner.get()
}


actual class URL {
    val url:NSURL

    actual constructor(urlStr:String) {
        url = NSURL(string=urlStr)
    }

    actual constructor(base: URL, relUrl: String) {
        url = NSURL.URLWithString(relUrl, base.url)!!
    }

    actual constructor(protocol: String, host: String, port: Int, file: String) {
        url = NSURL(protocol, "$host:$port", file)
    }

    actual fun getProtocol():String = url.scheme!!
    actual fun getHost():String = url.host!!
    actual fun getPort():Int = url.port!!.intValue
    actual fun getPath():String = url.path ?: ""
    actual fun getRef():String? = url.fragment
    actual fun getFile():String = url.fileReferenceURL()?.toString() ?: ""
    actual override fun toString():String = url.toString()
}

actual open class ThreadLocal<T>{
    private var inner:T?  = null

    actual constructor() {
    }

    actual fun get():T? {
        if (inner == null) {
            inner = initialValue()
        }
        return inner
    }

    actual fun set(value:T) {
        inner = value
    }

    actual open fun initialValue(): T? = null
}