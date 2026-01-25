package dev.freya02.botcommands.method.accessors.api

import dev.freya02.botcommands.method.accessors.api.annotations.ExperimentalMethodAccessorsApi

/**
 * Configuration object of method accessors.
 */
@ExperimentalMethodAccessorsApi
object MethodAccessorsConfig {
    /**
     * If enabled, instructs the framework to prefer using improved reflection calls, with the following benefits:
     * - Shorter stack traces in exceptions and the debugger
     * - No [InvocationTargetExceptions][java.lang.reflect.InvocationTargetException]
     * - Better performance
     *
     * This feature requires adding the `BotCommands-method-accessors-classfile` dependency
     * and *running* on Java 24+, if your bot doesn't fulfill these conditions, this is ignored.
     */
    @ExperimentalMethodAccessorsApi
    @get:JvmStatic
    @get:JvmName("isPreferClassFileAccessors")
    var preferClassFileAccessors: Boolean = false
        private set

    /**
     * Instructs the framework to prefer using improved reflection calls, with the following benefits:
     * - Shorter stack traces in exceptions and the debugger
     * - No [InvocationTargetExceptions][java.lang.reflect.InvocationTargetException]
     * - Better performance
     *
     * This feature requires adding the `BotCommands-method-accessors-classfile` dependency
     * and *running* on Java 24+, if your bot doesn't fulfill these conditions, this method has no effect.
     */
    @JvmStatic
    @ExperimentalMethodAccessorsApi
    fun preferClassFileAccessors() {
        preferClassFileAccessors = true
    }
}
