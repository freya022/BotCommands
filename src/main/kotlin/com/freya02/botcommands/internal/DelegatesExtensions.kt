package com.freya02.botcommands.internal

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class NotNullVar<T : Any>(private val message: String) : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException(message)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    fun test() {

    }
}

@Suppress("unused")
fun <T : Any> Delegates.notNull(message: String): NotNullVar<T> = NotNullVar(message)