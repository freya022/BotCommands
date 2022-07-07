package com.freya02.botcommands.internal

import com.freya02.botcommands.core.api.config.BConfig
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class LockableVar<T : Any?>(private val config: BConfig) : ReadWriteProperty<Any?, T> {
    protected var value: T? = null

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (config.locked) throwUser("This config is immutable")
        this.value = value
    }

    fun hasValue() = value != null
}

class NotNullVar<T : Any>(config: BConfig, private val message: String) : LockableVar<T>(config) {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException(message)
    }
}

class NullableVar<T : Any>(config: BConfig) : LockableVar<T?>(config) {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }
}

@Suppress("unused")
fun <T : Any> Delegates.lockableNotNull(config: BConfig, message: String): NotNullVar<T> = NotNullVar(config, message)

@Suppress("unused")
fun <T : Any> Delegates.lockable(config: BConfig): NullableVar<T> = NullableVar(config)