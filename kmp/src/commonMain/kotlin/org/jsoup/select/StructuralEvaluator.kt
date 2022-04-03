package org.jsoup.select

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Collector.FirstFinder

/**
 * Base structural evaluator.
 */
internal abstract class StructuralEvaluator : Evaluator() {
    lateinit var evaluator: Evaluator
}

internal class Root : Evaluator() {

    override fun matches(root: Element?, element: Element): Boolean {
        return root === element
    }
}

internal class Has(evaluator: Evaluator) : StructuralEvaluator() {

    val finder: FirstFinder

    init {
        this.evaluator = evaluator
        finder = FirstFinder(evaluator)
    }

    override fun matches(root: Element?, element: Element): Boolean {
        // for :has, we only want to match children (or below), not the input element. And we want to minimize GCs
        for (i in 0 until element.childNodeSize()) {
            val node: Node = element.childNode(i)
            if (node is Element) {
                val match = finder.find(element, node)
                if (match != null) return true
            }
        }
        return false
    }

    override fun toString(): String {
        return ":has($evaluator)"
    }
}

internal class Not(evaluator: Evaluator) : StructuralEvaluator() {

    init {
        this.evaluator = evaluator
    }

    override fun matches(root: Element?, element: Element): Boolean {
        return !evaluator.matches(root, element)
    }

    override fun toString(): String {
        return ":not($evaluator)"
    }
}

internal class Parent(evaluator: Evaluator) : StructuralEvaluator() {

    init {
        this.evaluator = evaluator
    }

    override fun matches(root: Element?, element: Element): Boolean {
        if (root === element) return false
        var parent = element.parent()
        while (parent != null) {
            if (evaluator.matches(root, parent)) return true
            if (parent === root) break
            parent = parent.parent()
        }
        return false
    }

    override fun toString(): String {
        return "$evaluator "
    }
}

internal class ImmediateParent(evaluator: Evaluator) : StructuralEvaluator() {

    init {
        this.evaluator = evaluator
    }

    override fun matches(root: Element?, element: Element): Boolean {
        if (root === element) return false
        val parent = element.parent()
        return parent != null && evaluator.matches(root, parent)
    }

    override fun toString(): String {
        return "$evaluator > "
    }
}

internal class PreviousSibling(evaluator: Evaluator) : StructuralEvaluator() {

    init {
        this.evaluator = evaluator
    }

    override fun matches(root: Element?, element: Element): Boolean {
        if (root === element) return false
        var prev = element.previousElementSibling()
        while (prev != null) {
            if (evaluator.matches(root, prev)) return true
            prev = prev.previousElementSibling()
        }
        return false
    }

    override fun toString(): String {
        return "$evaluator ~ "
    }
}

internal class ImmediatePreviousSibling(evaluator: Evaluator) : StructuralEvaluator() {

    init {
        this.evaluator = evaluator
    }

    override fun matches(root: Element?, element: Element): Boolean {
        if (root === element) return false
        val prev = element.previousElementSibling()
        return prev != null && evaluator.matches(root, prev)
    }

    override fun toString(): String {
        return "$evaluator + "
    }
}