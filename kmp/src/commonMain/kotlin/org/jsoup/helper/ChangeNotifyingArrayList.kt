package org.jsoup.helper

import kotlin.collections.ArrayList

/**
 * Implementation of ArrayList that watches out for changes to the contents.
 */
abstract class ChangeNotifyingArrayList<E>(initialCapacity: Int, val delegate:ArrayList<E> = ArrayList(initialCapacity)) : MutableList<E> by delegate {

    abstract fun onContentsChanged()
    override fun set(index: Int, element: E): E {
        onContentsChanged()
        return delegate.set(index, element)
    }

    override fun add(e: E): Boolean {
        onContentsChanged()
        return delegate.add(e)
    }

    override fun add(index: Int, element: E) {
        onContentsChanged()
        delegate.add(index, element)
    }

    override fun removeAt(index: Int): E {
        onContentsChanged()
        return delegate.removeAt(index)
    }

    override fun remove(o: E): Boolean {
        onContentsChanged()
        return delegate.remove(o)
    }

    override fun clear() {
        onContentsChanged()
        delegate.clear()
    }

    override fun addAll(c: Collection<E>): Boolean {
        onContentsChanged()
        return delegate.addAll(c)
    }

    override fun addAll(index: Int, c: Collection<E>): Boolean {
        onContentsChanged()
        return delegate.addAll(index, c)
    }

    override fun removeAll(c: Collection<E>): Boolean {
        onContentsChanged()
        return delegate.removeAll(c)
    }

    override fun retainAll(c: Collection<E>): Boolean {
        onContentsChanged()
        return delegate.retainAll(c)
    }
}