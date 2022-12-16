package com.freya02.botcommands.internal

import com.freya02.botcommands.api.core.config.BConfig
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class LockableVar<T : Any?>(private val config: BConfig, defaultVal: T?) : ReadWriteProperty<Any?, T> {
    protected var value: T? = defaultVal

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (config.locked) throwUser("This config is immutable")
        this.value = value
    }

    fun hasValue() = value != null
}

class NotNullVar<T : Any>(config: BConfig, private val message: String, defaultVal: T?) : LockableVar<T>(config, defaultVal) {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException(message)
    }
}

class NullableVar<T : Any>(config: BConfig, defaultVal: T?) : LockableVar<T?>(config, defaultVal) {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }
}

@Suppress("UnusedReceiverParameter")
fun <T : Any> Delegates.lockableNotNull(config: BConfig, message: String = "This property cannot be null", defaultVal: T? = null): NotNullVar<T> = NotNullVar(config, message, defaultVal)

@Suppress("UnusedReceiverParameter")
fun <T : Any> Delegates.lockable(config: BConfig, defaultVal: T? = null): NullableVar<T> = NullableVar(config, defaultVal)