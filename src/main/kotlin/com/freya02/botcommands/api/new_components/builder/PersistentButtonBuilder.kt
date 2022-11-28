package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.internal.new_components.*
import com.freya02.botcommands.internal.new_components.new.PersistentTimeout
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

@OptIn(ExperimentalTime::class)
interface PersistentButtonBuilder : ButtonBuilder<PersistentButtonBuilder> {
    override val timeout: PersistentTimeout?
    override val handler: PersistentHandler?

    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handlerName: String, vararg args: Any?): PersistentButtonBuilder =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()), handlerName, *args)

    @JvmSynthetic
    fun timeout(timeout: Duration, handlerName: String, vararg args: Any?): PersistentButtonBuilder

    fun bindTo(handlerName: String, vararg data: Any?): PersistentButtonBuilder
}