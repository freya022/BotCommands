package io.github.freya022.botcommands.internal.core.reflection

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

private typealias MutableParameterEntry = MutableMap.MutableEntry<KParameter, Any?>

private val NO_VALUE = Any()

internal class ParameterMap(function: KFunction<*>) : AbstractMutableMap<KParameter, Any?>(),
                                                      MutableMap<KParameter, Any?> {

    private val parameters = function.parameters
    private val _values = Array<Any?>(parameters.size) { NO_VALUE }

    override val entries: MutableSet<MutableParameterEntry>
        get() = Set()

    override fun containsKey(key: KParameter): Boolean {
        return _values[key.index] !== NO_VALUE
    }

    override fun get(key: KParameter): Any? {
        return _values[key.index]
    }

    override fun put(key: KParameter, value: Any?): Any? {
        val oldVal = _values[key.index]
        _values[key.index] = value
        return oldVal
    }

    private inner class Set : AbstractMutableSet<MutableParameterEntry>() {
        override fun add(element: MutableParameterEntry): Boolean {
            return put(element.key, element.value) != element.value
        }

        override val size: Int
            get() = _values.count { it !== NO_VALUE }

        override fun iterator(): MutableIterator<MutableParameterEntry> {
            return Iterator()
        }
    }

    private inner class Iterator : MutableIterator<MutableParameterEntry> {
        private var index = 0

        override fun hasNext(): Boolean {
            for (i in index..<size) {
                if (_values[i] !== NO_VALUE) {
                    index = i
                    return true
                }
            }
            return false
        }

        override fun next(): MutableParameterEntry {
            return Entry(index++)
        }

        override fun remove() {
            _values[index - 1] = NO_VALUE
        }
    }

    private inner class Entry(private val index: Int) : MutableParameterEntry {
        override val key: KParameter
            get() = parameters[index]
        override val value: Any?
            get() = _values[index]

        override fun setValue(newValue: Any?): Any? {
            return put(key, newValue)
        }
    }
}

internal inline fun buildParameters(function: KFunction<*>, block: ParameterMap.() -> Unit): Map<KParameter, Any?> {
    return ParameterMap(function).apply(block)
}