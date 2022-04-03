/*
 * Copyright (c) 2000, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package kotlinx.util

import kotlin.collections.MutableMap.MutableEntry
import kotlin.jvm.Transient

/**
 * This class implements the <tt>Map</tt> interface with a hash table, using
 * reference-equality in place of object-equality when comparing keys (and
 * values).  In other words, in an <tt>IdentityHashMap</tt>, two keys
 * <tt>k1</tt> and <tt>k2</tt> are considered equal if and only if
 * <tt>(k1==k2)</tt>.  (In normal <tt>Map</tt> implementations (like
 * <tt>HashMap</tt>) two keys <tt>k1</tt> and <tt>k2</tt> are considered equal
 * if and only if <tt>(k1==null ? k2==null : k1.equals(k2))</tt>.)
 *
 *
 * **This class is *not* a general-purpose <tt>Map</tt>
 * implementation!  While this class implements the <tt>Map</tt> interface, it
 * intentionally violates <tt>Map's</tt> general contract, which mandates the
 * use of the <tt>equals</tt> method when comparing objects.  This class is
 * designed for use only in the rare cases wherein reference-equality
 * semantics are required.**
 *
 *
 * A typical use of this class is *topology-preserving object graph
 * transformations*, such as serialization or deep-copying.  To perform such
 * a transformation, a program must maintain a "node table" that keeps track
 * of all the object references that have already been processed.  The node
 * table must not equate distinct objects even if they happen to be equal.
 * Another typical use of this class is to maintain *proxy objects*.  For
 * example, a debugging facility might wish to maintain a proxy object for
 * each object in the program being debugged.
 *
 *
 * This class provides all of the optional map operations, and permits
 * <tt>null</tt> values and the <tt>null</tt> key.  This class makes no
 * guarantees as to the order of the map; in particular, it does not guarantee
 * that the order will remain constant over time.
 *
 *
 * This class provides constant-time performance for the basic
 * operations (<tt>get</tt> and <tt>put</tt>), assuming the system
 * identity hash function ([System.identityHashCode])
 * disperses elements properly among the buckets.
 *
 *
 * This class has one tuning parameter (which affects performance but not
 * semantics): *expected maximum size*.  This parameter is the maximum
 * number of key-value mappings that the map is expected to hold.  Internally,
 * this parameter is used to determine the number of buckets initially
 * comprising the hash table.  The precise relationship between the expected
 * maximum size and the number of buckets is unspecified.
 *
 *
 * If the size of the map (the number of key-value mappings) sufficiently
 * exceeds the expected maximum size, the number of buckets is increased.
 * Increasing the number of buckets ("rehashing") may be fairly expensive, so
 * it pays to create identity hash maps with a sufficiently large expected
 * maximum size.  On the other hand, iteration over collection views requires
 * time proportional to the number of buckets in the hash table, so it
 * pays not to set the expected maximum size too high if you are especially
 * concerned with iteration performance or memory usage.
 *
 *
 * **Note that this implementation is not synchronized.**
 * If multiple threads access an identity hash map concurrently, and at
 * least one of the threads modifies the map structurally, it *must*
 * be synchronized externally.  (A structural modification is any operation
 * that adds or deletes one or more mappings; merely changing the value
 * associated with a key that an instance already contains is not a
 * structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the map.
 *
 * If no such object exists, the map should be "wrapped" using the
 * [Collections.synchronizedMap]
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the map:<pre>
 * Map m = Collections.synchronizedMap(new IdentityHashMap(...));</pre>
 *
 *
 * The iterators returned by the <tt>iterator</tt> method of the
 * collections returned by all of this class's "collection view
 * methods" are *fail-fast*: if the map is structurally modified
 * at any time after the iterator is created, in any way except
 * through the iterator's own <tt>remove</tt> method, the iterator
 * will throw a [ConcurrentModificationException].  Thus, in the
 * face of concurrent modification, the iterator fails quickly and
 * cleanly, rather than risking arbitrary, non-deterministic behavior
 * at an undetermined time in the future.
 *
 *
 * Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: *fail-fast iterators should be used only
 * to detect bugs.*
 *
 *
 * Implementation note: This is a simple *linear-probe* hash table,
 * as described for example in texts by Sedgewick and Knuth.  The array
 * alternates holding keys and values.  (This has better locality for large
 * tables than does using separate arrays.)  For many JRE implementations
 * and operation mixes, this class will yield better performance than
 * [HashMap] (which uses *chaining* rather than linear-probing).
 *
 *
 * This class is a member of the
 * [
 * Java Collections Framework]({@docRoot}/../technotes/guides/collections/index.html).
 *
 * @see System.identityHashCode
 * @see Object.hashCode
 * @see Collection
 *
 * @see Map
 *
 * @see HashMap
 *
 * @see TreeMap
 *
 * @author  Doug Lea and Josh Bloch
 * @since   1.4
 */
class IdentityHashMap<K, V> : AbstractMutableMap<K, V> {

    /**
     * The table, resized as necessary. Length MUST always be a power of two.
     */
    @Transient
    lateinit var table: Array<Any?> // non-private to simplify nested class access


    /**
     * The number of key-value mappings contained in this identity hash map.
     *
     * @serial
     */
    override var size = 0

    /**
     * The number of modifications, to support fast-fail iterators
     */
    @Transient
    var modCount = 0

    /**
     * Constructs a new, empty identity hash map with a default expected
     * maximum size (21).
     */
    constructor() {
        init(IdentityHashMap.Companion.DEFAULT_CAPACITY)
    }

    /**
     * Constructs a new, empty map with the specified expected maximum size.
     * Putting more than the expected number of key-value mappings into
     * the map may cause the internal data structure to grow, which may be
     * somewhat time-consuming.
     *
     * @param expectedMaxSize the expected maximum size of the map
     * @throws IllegalArgumentException if <tt>expectedMaxSize</tt> is negative
     */
    constructor(expectedMaxSize: Int) {
        if (expectedMaxSize < 0) throw IllegalArgumentException(
            "expectedMaxSize is negative: "
                    + expectedMaxSize
        )
        init(IdentityHashMap.Companion.capacity(expectedMaxSize))
    }

    /**
     * Initializes object to be an empty map with the specified initial
     * capacity, which is assumed to be a power of two between
     * MINIMUM_CAPACITY and MAXIMUM_CAPACITY inclusive.
     */
    private fun init(initCapacity: Int) {
        // assert (initCapacity & -initCapacity) == initCapacity; // power of 2
        // assert initCapacity >= MINIMUM_CAPACITY;
        // assert initCapacity <= MAXIMUM_CAPACITY;
        table = arrayOfNulls(2 * initCapacity)
    }

    /**
     * Constructs a new identity hash map containing the keys-value mappings
     * in the specified map.
     *
     * @param m the map whose mappings are to be placed into this map
     * @throws NullPointerException if the specified map is null
     */
    constructor(m: Map<out K, out V>) : this(((1 + m.size) * 1.1).toInt()) {
        // Allow for a bit of growth
        putAll(m)
    }

    /**
     * Returns <tt>true</tt> if this identity hash map contains no key-value
     * mappings.
     *
     * @return <tt>true</tt> if this identity hash map contains no key-value
     * mappings
     */
    override fun isEmpty(): Boolean {
        return size == 0
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or `null` if this map contains no mapping for the key.
     *
     *
     * More formally, if this map contains a mapping from a key
     * `k` to a value `v` such that `(key == k)`,
     * then this method returns `v`; otherwise it returns
     * `null`.  (There can be at most one such mapping.)
     *
     *
     * A return value of `null` does not *necessarily*
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to `null`.
     * The [containsKey][.containsKey] operation may be used to
     * distinguish these two cases.
     *
     * @see .put
     */
    override fun get(key: K): V? {
        val k: Any = maskNull(key)
        val tab = table
        val len = tab.size
        var i: Int = hash(k, len)
        while (true) {
            val item = tab[i]
            if (item === k) return tab[i + 1] as V
            if (item == null) return null
            i = IdentityHashMap.Companion.nextKeyIndex(i, len)
        }
    }

    /**
     * Tests whether the specified object reference is a key in this identity
     * hash map.
     *
     * @param   key   possible key
     * @return  `true` if the specified object reference is a key
     * in this map
     * @see .containsValue
     */
    override fun containsKey(key: K): Boolean {
        val k: Any = maskNull(key)
        val tab = table
        val len = tab.size
        var i: Int = hash(k, len)
        while (true) {
            val item = tab[i]
            if (item === k) return true
            if (item == null) return false
            i = IdentityHashMap.Companion.nextKeyIndex(i, len)
        }
    }

    /**
     * Tests whether the specified object reference is a value in this identity
     * hash map.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     * specified object reference
     * @see .containsKey
     */
    override fun containsValue(value: V): Boolean {
        val tab = table
        var i = 1
        while (i < tab.size) {
            if (tab[i] === value && tab[i - 1] != null) return true
            i += 2
        }
        return false
    }

    /**
     * Tests if the specified key-value mapping is in the map.
     *
     * @param   key   possible key
     * @param   value possible value
     * @return  `true` if and only if the specified key-value
     * mapping is in the map
     */
    private fun containsMapping(key: Any, value: Any?): Boolean {
        val k = maskNull(key)
        val tab = table
        val len = tab.size
        var i: Int = hash(k, len)
        while (true) {
            val item = tab[i]
            if (item === k) return tab[i + 1] === value
            if (item == null) return false
            i = IdentityHashMap.Companion.nextKeyIndex(i, len)
        }
    }

    /**
     * Associates the specified value with the specified key in this identity
     * hash map.  If the map previously contained a mapping for the key, the
     * old value is replaced.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the map
     * previously associated <tt>null</tt> with <tt>key</tt>.)
     * @see Object.equals
     * @see .get
     * @see .containsKey
     */
    override fun put(key: K, value: V): V? {
        val k: Any = maskNull(key)
        retryAfterResize@ while (true) {
            val tab = table
            val len = tab.size
            var i: Int = hash(k, len)
            var item: Any?
            while ((tab[i].also { item = it }) != null) {
                if (item === k) {
                    val oldValue = tab[i + 1] as V?
                    tab[i + 1] = value
                    return oldValue
                }
                i = IdentityHashMap.Companion.nextKeyIndex(i, len)
            }
            val s = size + 1
            // Use optimized form of 3 * s.
            // Next capacity is len, 2 * current capacity.
            if (s + (s shl 1) > len && resize(len)) {
                continue@retryAfterResize
            }
            modCount++
            tab[i] = k
            tab[i + 1] = value
            size = s
            return null
        }
    }

    /**
     * Resizes the table if necessary to hold given capacity.
     *
     * @param newCapacity the new capacity, must be a power of two.
     * @return whether a resize did in fact take place
     */
    private fun resize(newCapacity: Int): Boolean {
        // assert (newCapacity & -newCapacity) == newCapacity; // power of 2
        val newLength = newCapacity * 2
        val oldTable = table
        val oldLength = oldTable.size
        if (oldLength == 2 * MAXIMUM_CAPACITY) { // can't expand any further
            if (size == MAXIMUM_CAPACITY - 1) throw IllegalStateException("Capacity exhausted.")
            return false
        }
        if (oldLength >= newLength) return false
        val newTable = arrayOfNulls<Any>(newLength)
        var j = 0
        while (j < oldLength) {
            val key = oldTable[j]
            if (key != null) {
                val value = oldTable[j + 1]
                oldTable[j] = null
                oldTable[j + 1] = null
                var i: Int = hash(key, newLength)
                while (newTable[i] != null) i = IdentityHashMap.Companion.nextKeyIndex(i, newLength)
                newTable[i] = key
                newTable[i + 1] = value
            }
            j += 2
        }
        table = newTable
        return true
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    override fun putAll(m: Map<out K, out V>) {
        val n = m.size
        if (n == 0) return
        if (n > size) resize(IdentityHashMap.Companion.capacity(n)) // conservatively pre-expand
        for (e: Map.Entry<out K, out V> in m.entries) put(e.key, e.value)
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the map
     * previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    override fun remove(key: K): V? {
        val k: Any = maskNull(key)
        val tab = table
        val len = tab.size
        var i: Int = hash(k, len)
        while (true) {
            val item = tab[i]
            if (item === k) {
                modCount++
                size--
                val oldValue = tab[i + 1] as V?
                tab[i + 1] = null
                tab[i] = null
                closeDeletion(i)
                return oldValue
            }
            if (item == null) return null
            i = IdentityHashMap.Companion.nextKeyIndex(i, len)
        }
    }

    /**
     * Removes the specified key-value mapping from the map if it is present.
     *
     * @param   key   possible key
     * @param   value possible value
     * @return  `true` if and only if the specified key-value
     * mapping was in the map
     */
    private fun removeMapping(key: Any, value: Any?): Boolean {
        val k: Any = maskNull(key)
        val tab = table
        val len = tab.size
        var i: Int = hash(k, len)
        while (true) {
            val item = tab[i]
            if (item === k) {
                if (tab[i + 1] !== value) return false
                modCount++
                size--
                tab[i] = null
                tab[i + 1] = null
                closeDeletion(i)
                return true
            }
            if (item == null) return false
            i = IdentityHashMap.Companion.nextKeyIndex(i, len)
        }
    }

    /**
     * Rehash all possibly-colliding entries following a
     * deletion. This preserves the linear-probe
     * collision properties required by get, put, etc.
     *
     * @param d the index of a newly empty deleted slot
     */
    private fun closeDeletion(d: Int) {
        // Adapted from Knuth Section 6.4 Algorithm R
        var d = d
        val tab = table
        val len = tab.size

        // Look for items to swap into newly vacated slot
        // starting at index immediately following deletion,
        // and continuing until a null slot is seen, indicating
        // the end of a run of possibly-colliding keys.
        var item: Any?
        var i: Int = IdentityHashMap.Companion.nextKeyIndex(d, len)
        while ((tab[i].also { item = it }) != null) {

            // The following test triggers if the item at slot i (which
            // hashes to be at slot r) should take the spot vacated by d.
            // If so, we swap it in, and then continue with d now at the
            // newly vacated i.  This process will terminate when we hit
            // the null slot at the end of this run.
            // The test is messy because we are using a circular table.
            val r: Int = hash(item!!, len)
            if ((i < r && (r <= d || d <= i)) || (r <= d && d <= i)) {
                tab[d] = item
                tab[d + 1] = tab[i + 1]
                tab[i] = null
                tab[i + 1] = null
                d = i
            }
            i = IdentityHashMap.Companion.nextKeyIndex(i, len)
        }
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    override fun clear() {
        modCount++
        val tab = table
        for (i in tab.indices) tab[i] = null
        size = 0
    }

    /**
     * Compares the specified object with this map for equality.  Returns
     * <tt>true</tt> if the given object is also a map and the two maps
     * represent identical object-reference mappings.  More formally, this
     * map is equal to another map <tt>m</tt> if and only if
     * <tt>this.entrySet().equals(m.entrySet())</tt>.
     *
     *
     * **Owing to the reference-equality-based semantics of this map it is
     * possible that the symmetry and transitivity requirements of the
     * <tt>Object.equals</tt> contract may be violated if this map is compared
     * to a normal map.  However, the <tt>Object.equals</tt> contract is
     * guaranteed to hold among <tt>IdentityHashMap</tt> instances.**
     *
     * @param  other object to be compared for equality with this map
     * @return <tt>true</tt> if the specified object is equal to this map
     * @see Object.equals
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        } else if (other is IdentityHashMap<*, *>) {
            val m = other
            if (m.size != size) return false
            val tab: Array<Any?> = m.table
            var i = 0
            while (i < tab.size) {
                val k = tab[i]
                if (k != null && !containsMapping(k, tab[i + 1])) return false
                i += 2
            }
            return true
        } else if (other is Map<*, *>) {
            return (entries == other.entries)
        } else {
            return false // o is not a Map
        }
    }

    /**
     * Returns the hash code value for this map.  The hash code of a map is
     * defined to be the sum of the hash codes of each entry in the map's
     * <tt>entrySet()</tt> view.  This ensures that <tt>m1.equals(m2)</tt>
     * implies that <tt>m1.hashCode()==m2.hashCode()</tt> for any two
     * <tt>IdentityHashMap</tt> instances <tt>m1</tt> and <tt>m2</tt>, as
     * required by the general contract of [Object.hashCode].
     *
     *
     * **Owing to the reference-equality-based semantics of the
     * <tt>Map.Entry</tt> instances in the set returned by this map's
     * <tt>entrySet</tt> method, it is possible that the contractual
     * requirement of <tt>Object.hashCode</tt> mentioned in the previous
     * paragraph will be violated if one of the two objects being compared is
     * an <tt>IdentityHashMap</tt> instance and the other is a normal map.**
     *
     * @return the hash code value for this map
     * @see Object.equals
     * @see .equals
     */
    override fun hashCode(): Int {
        var result = 0
        val tab = table
        var i = 0
        while (i < tab.size) {
            val key = tab[i]
            if (key != null) {
                val k = unmaskNull(key)
                result += identityHashCode(k) xor
                        identityHashCode(tab[i + 1])
            }
            i += 2
        }
        return result
    }

    private abstract inner class IdentityHashMapIterator<T>() : MutableIterator<T> {

        var index = (if (size != 0) 0 else table.size) // current slot.
        var expectedModCount = modCount // to support fast-fail
        var lastReturnedIndex = -1 // to allow remove()
        var indexValid // To avoid unnecessary next computation
                = false
        var traversalTable: Array<Any?> = table // reference to main table or copy
        override fun hasNext(): Boolean {
            val tab = traversalTable
            var i = index
            while (i < tab.size) {
                val key: Any? = tab[i]
                if (key != null) {
                    index = i
                    return true.also { indexValid = it }
                }
                i += 2
            }
            index = tab.size
            return false
        }

        protected fun nextIndex(): Int {
            if (modCount != expectedModCount) throw ConcurrentModificationException()
            if (!indexValid && !hasNext()) throw NoSuchElementException()
            indexValid = false
            lastReturnedIndex = index
            index += 2
            return lastReturnedIndex
        }

        override fun remove() {
            if (lastReturnedIndex == -1) throw IllegalStateException()
            if (modCount != expectedModCount) throw ConcurrentModificationException()
            expectedModCount = ++modCount
            val deletedSlot = lastReturnedIndex
            lastReturnedIndex = -1
            // back up index to revisit new contents after deletion
            index = deletedSlot
            indexValid = false

            // Removal code proceeds as in closeDeletion except that
            // it must catch the rare case where an element already
            // seen is swapped into a vacant slot that will be later
            // traversed by this iterator. We cannot allow future
            // next() calls to return it again.  The likelihood of
            // this occurring under 2/3 load factor is very slim, but
            // when it does happen, we must make a copy of the rest of
            // the table to use for the rest of the traversal. Since
            // this can only happen when we are near the end of the table,
            // even in these rare cases, this is not very expensive in
            // time or space.
            val tab: Array<Any?> = traversalTable
            val len = tab.size
            var d = deletedSlot
            val key = tab[d]
            tab[d] = null // vacate the slot
            tab[d + 1] = null

            // If traversing a copy, remove in real table.
            // We can skip gap-closure on copy.
            if (tab != table) {
                this@IdentityHashMap.remove(key)
                expectedModCount = modCount
                return
            }
            size--
            var item: Any?
            var i: Int = IdentityHashMap.Companion.nextKeyIndex(d, len)
            while ((tab[i].also { item = it }) != null) {
                val r: Int = hash(item!!, len)
                // See closeDeletion for explanation of this conditional
                if ((i < r && (r <= d || d <= i)) || (r <= d && d <= i)) {

                    // If we are about to swap an already-seen element
                    // into a slot that may later be returned by next(),
                    // then clone the rest of table for use in future
                    // next() calls. It is OK that our copy will have
                    // a gap in the "wrong" place, since it will never
                    // be used for searching anyway.
                    if ((i < deletedSlot) && (d >= deletedSlot) && (traversalTable == table)) {
                        val remaining = len - deletedSlot
                        val newTable = arrayOfNulls<Any>(remaining)
                        tab.copyInto(newTable, 0, deletedSlot, deletedSlot + remaining)
                        traversalTable = newTable
                        index = 0
                    }
                    tab[d] = item
                    tab[d + 1] = tab[i + 1]
                    tab[i] = null
                    tab[i + 1] = null
                    d = i
                }
                i = IdentityHashMap.Companion.nextKeyIndex(i, len)
            }
        }
    }

    private inner class KeyIterator() : IdentityHashMapIterator<K>() {

        override fun next(): K {
            return unmaskNull(traversalTable[nextIndex()]!!) as K
        }
    }

    private inner class ValueIterator() : IdentityHashMapIterator<V>() {

        override fun next(): V {
            return traversalTable[nextIndex() + 1] as V
        }
    }

    private inner class EntryIterator() : IdentityHashMapIterator<MutableEntry<K, V>>() {

        private var lastReturnedEntry: EntryIterator.Entry? = null
        override fun next(): MutableEntry<K, V> {
            lastReturnedEntry = this@EntryIterator.Entry(nextIndex())
            return lastReturnedEntry as Entry
        }

        override fun remove() {
            lastReturnedIndex = (if ((null == lastReturnedEntry)) -1 else lastReturnedEntry!!.index)
            super.remove()
            lastReturnedEntry!!.index = lastReturnedIndex
            lastReturnedEntry = null
        }

        private inner class Entry constructor(var index: Int) : MutableEntry<K, V> {

            override val key: K
                get() {
                    checkIndexForEntryUse()
                    return unmaskNull(traversalTable[index]!!) as K
                }
            override val value: V
                get() {
                    checkIndexForEntryUse()
                    return traversalTable[index + 1] as V
                }

            override fun setValue(value: V): V {
                checkIndexForEntryUse()
                val oldValue = traversalTable[index + 1] as V
                traversalTable[index + 1] = value
                // if shadowing, force into main table
                if (traversalTable !== table) put(traversalTable[index] as K, value)
                return oldValue
            }

            override fun equals(o: Any?): Boolean {
                if (index < 0) return super.equals(o)
                if (o !is Map.Entry<*, *>) return false
                val e: Map.Entry<*, *> = o
                return (e.key === unmaskNull(traversalTable[index]!!) && e.value === traversalTable[index + 1])
            }

            override fun hashCode(): Int {
                return if (lastReturnedIndex < 0) {
                    super.hashCode()
                } else {
                    (identityHashCode(unmaskNull(traversalTable.get(index)!!)) xor identityHashCode(traversalTable.get(index + 1)))
                }
            }

            override fun toString(): String {
                return if (index < 0) {
                    super.toString()
                } else {
                    ((unmaskNull(traversalTable.get(index)!!).toString() + "=" + traversalTable.get(index + 1)))
                }
            }

            private fun checkIndexForEntryUse() {
                if (index < 0) throw IllegalStateException("Entry was removed")
            }
        }
    }

    /**
     * Returns an identity-based set view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are reflected in
     * the set, and vice-versa.  If the map is modified while an iteration
     * over the set is in progress, the results of the iteration are
     * undefined.  The set supports element removal, which removes the
     * corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> methods.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> methods.
     *
     *
     * **While the object returned by this method implements the
     * <tt>Set</tt> interface, it does *not* obey <tt>Set's</tt> general
     * contract.  Like its backing map, the set returned by this method
     * defines element equality as reference-equality rather than
     * object-equality.  This affects the behavior of its <tt>contains</tt>,
     * <tt>remove</tt>, <tt>containsAll</tt>, <tt>equals</tt>, and
     * <tt>hashCode</tt> methods.**
     *
     *
     * **The <tt>equals</tt> method of the returned set returns <tt>true</tt>
     * only if the specified object is a set containing exactly the same
     * object references as the returned set.  The symmetry and transitivity
     * requirements of the <tt>Object.equals</tt> contract may be violated if
     * the set returned by this method is compared to a normal set.  However,
     * the <tt>Object.equals</tt> contract is guaranteed to hold among sets
     * returned by this method.**
     *
     *
     * The <tt>hashCode</tt> method of the returned set returns the sum of
     * the *identity hashcodes* of the elements in the set, rather than
     * the sum of their hashcodes.  This is mandated by the change in the
     * semantics of the <tt>equals</tt> method, in order to enforce the
     * general contract of the <tt>Object.hashCode</tt> method among sets
     * returned by this method.
     *
     * @return an identity-based set view of the keys contained in this map
     * @see Object.equals
     * @see System.identityHashCode
     */
    @Transient
    var keySetImpl: MutableSet<K>? = null

    override val keys: MutableSet<K>
        get() {
            var ks = keySetImpl
            if (ks == null) {
                ks = this@IdentityHashMap.KeySet()
                keySetImpl = ks
            }
            return ks
        }

    private inner class KeySet() : AbstractMutableSet<K>() {

        override fun iterator(): MutableIterator<K> {
            return this@IdentityHashMap.KeyIterator()
        }

        override val size: Int
            get() = this@IdentityHashMap.size

        override operator fun contains(element: K): Boolean {
            return containsKey(element)
        }

        override fun remove(element: K): Boolean {
            val oldSize = size
            this@IdentityHashMap.remove(element)
            return size != oldSize
        }

        /*
         * Must revert from AbstractSet's impl to AbstractCollection's, as
         * the former contains an optimization that results in incorrect
         * behavior when c is a smaller "normal" (non-identity-based) Set.
         */
        override fun removeAll(elements: Collection<K>): Boolean {
            var modified = false
            val i = iterator()
            while (i.hasNext()) {
                if (elements.contains(i.next())) {
                    i.remove()
                    modified = true
                }
            }
            return modified
        }

        override fun clear() {
            this@IdentityHashMap.clear()
        }

        override fun hashCode(): Int {
            var result = 0
            for (key: K in this) result += identityHashCode(key)
            return result
        }

        override fun add(element: K): Boolean {
           throw UnsupportedOperationException()
        }
    }

    /**
     * Returns a [Collection] view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress,
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> methods.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> methods.
     *
     *
     * **While the object returned by this method implements the
     * <tt>Collection</tt> interface, it does *not* obey
     * <tt>Collection's</tt> general contract.  Like its backing map,
     * the collection returned by this method defines element equality as
     * reference-equality rather than object-equality.  This affects the
     * behavior of its <tt>contains</tt>, <tt>remove</tt> and
     * <tt>containsAll</tt> methods.**
     */
    @Transient
    var valuesImpl: MutableCollection<V>? = null
    override val values: MutableCollection<V>
        get() {
            var vs = valuesImpl
            if (vs == null) {
                vs = this@IdentityHashMap.Values()
                valuesImpl = vs
            }
            return vs
        }

    private inner class Values() : AbstractMutableCollection<V>() {

        override fun iterator(): MutableIterator<V> {
            return this@IdentityHashMap.ValueIterator()
        }

        override val size: Int
            get() = this@IdentityHashMap.size

        override operator fun contains(o: V): Boolean {
            return containsValue(o)
        }

        override fun remove(o: V): Boolean {
            val i = iterator()
            while (i.hasNext()) {
                if (i.next() === o) {
                    i.remove()
                    return true
                }
            }
            return false
        }

        override fun clear() {
            this@IdentityHashMap.clear()
        }

        override fun add(element: V): Boolean {
            throw UnsupportedOperationException()
        }
    }

    /**
     * Returns a [Set] view of the mappings contained in this map.
     * Each element in the returned set is a reference-equality-based
     * <tt>Map.Entry</tt>.  The set is backed by the map, so changes
     * to the map are reflected in the set, and vice-versa.  If the
     * map is modified while an iteration over the set is in progress,
     * the results of the iteration are undefined.  The set supports
     * element removal, which removes the corresponding mapping from
     * the map, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt>
     * methods.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> methods.
     *
     *
     * Like the backing map, the <tt>Map.Entry</tt> objects in the set
     * returned by this method define key and value equality as
     * reference-equality rather than object-equality.  This affects the
     * behavior of the <tt>equals</tt> and <tt>hashCode</tt> methods of these
     * <tt>Map.Entry</tt> objects.  A reference-equality based <tt>Map.Entry
     * e</tt> is equal to an object <tt>o</tt> if and only if <tt>o</tt> is a
     * <tt>Map.Entry</tt> and <tt>e.getKey()==o.getKey() &amp;&amp;
     * e.getValue()==o.getValue()</tt>.  To accommodate these equals
     * semantics, the <tt>hashCode</tt> method returns
     * <tt>System.identityHashCode(e.getKey()) ^
     * System.identityHashCode(e.getValue())</tt>.
     *
     *
     * **Owing to the reference-equality-based semantics of the
     * <tt>Map.Entry</tt> instances in the set returned by this method,
     * it is possible that the symmetry and transitivity requirements of
     * the [Object.equals] contract may be violated if any of
     * the entries in the set is compared to a normal map entry, or if
     * the set returned by this method is compared to a set of normal map
     * entries (such as would be returned by a call to this method on a normal
     * map).  However, the <tt>Object.equals</tt> contract is guaranteed to
     * hold among identity-based map entries, and among sets of such entries.
     ** *
     *
     * @return a set view of the identity-mappings contained in this map
     */
    @Transient
    var entriesImpl: MutableSet<MutableEntry<K, V>>? = null
    override val entries: MutableSet<MutableEntry<K, V>>
        get() {
            val es = entriesImpl
            return if (es != null) {
                es
            } else {
                this@IdentityHashMap.EntrySet().also {
                    entriesImpl = it
                }
            }
        }

    private inner class EntrySet() : AbstractMutableSet<MutableEntry<K, V>>() {

        override fun iterator(): MutableIterator<MutableEntry<K, V>> {
            return this@IdentityHashMap.EntryIterator()
        }

        override fun contains(element: MutableEntry<K, V>): Boolean {
            val entry: Map.Entry<*, *> = element
            return containsMapping(entry.key!!, entry.value)
        }

        override fun remove(element: MutableEntry<K, V>): Boolean {
            val entry: Map.Entry<*, *> = element
            return removeMapping(entry.key!!, entry.value)
        }

        override val size: Int
            get() = this@IdentityHashMap.size

        /*
         * Must revert from AbstractSet's impl to AbstractCollection's, as
         * the former contains an optimization that results in incorrect
         * behavior when c is a smaller "normal" (non-identity-based) Set.
         */
        override fun removeAll(elements: Collection<MutableEntry<K, V>>): Boolean {
            var modified = false
            val i = iterator()
            while (i.hasNext()) {
                if (elements.contains(i.next())) {
                    i.remove()
                    modified = true
                }
            }
            return modified
        }

        override fun add(element: MutableEntry<K, V>): Boolean {
            throw UnsupportedOperationException()
        }
    }

    companion object {

        /**
         * The initial capacity used by the no-args constructor.
         * MUST be a power of two.  The value 32 corresponds to the
         * (specified) expected maximum size of 21, given a load factor
         * of 2/3.
         */
        private val DEFAULT_CAPACITY = 32

        /**
         * The minimum capacity, used if a lower value is implicitly specified
         * by either of the constructors with arguments.  The value 4 corresponds
         * to an expected maximum size of 2, given a load factor of 2/3.
         * MUST be a power of two.
         */
        private val MINIMUM_CAPACITY = 4

        /**
         * The maximum capacity, used if a higher value is implicitly specified
         * by either of the constructors with arguments.
         * MUST be a power of two <= 1<<29.
         *
         * In fact, the map can hold no more than MAXIMUM_CAPACITY-1 items
         * because it has to have at least one slot with the key == null
         * in order to avoid infinite loops in get(), put(), remove()
         */
        private val MAXIMUM_CAPACITY = 1 shl 29

        /**
         * Value representing null keys inside tables.
         */
        val NULL_KEY = Any()

        /**
         * Use NULL_KEY for key if it is null.
         */
        private fun maskNull(key: Any?): Any {
            return (key ?: NULL_KEY)
        }

        /**
         * Returns internal representation of null key back to caller as null.
         */
        private fun unmaskNull(key: Any): Any? {
            return (if (key === NULL_KEY) null else key)
        }

        /**
         * Returns the appropriate capacity for the given expected maximum size.
         * Returns the smallest power of two between MINIMUM_CAPACITY and
         * MAXIMUM_CAPACITY, inclusive, that is greater than (3 *
         * expectedMaxSize)/2, if such a number exists.  Otherwise returns
         * MAXIMUM_CAPACITY.
         */
        private fun capacity(expectedMaxSize: Int): Int {
            // assert expectedMaxSize >= 0;
            return if ((expectedMaxSize > MAXIMUM_CAPACITY / 3)) {
                MAXIMUM_CAPACITY
            } else if ((expectedMaxSize <= 2 * MINIMUM_CAPACITY / 3)) {
                MINIMUM_CAPACITY
            } else {
                (expectedMaxSize + (expectedMaxSize shl 1)).highestOneBit()
            }
        }

        /**
         * Returns index for Object x.
         */
        private fun hash(x: Any, length: Int): Int {
            val h = identityHashCode(x)
            // Multiply by -127, and left-shift to use least bit as part of hash
            return ((h shl 1) - (h shl 8)) and (length - 1)
        }

        /**
         * Circularly traverses table of size len.
         */
        private fun nextKeyIndex(i: Int, len: Int): Int {
            return (if (i + 2 < len) i + 2 else 0)
        }
    }
}