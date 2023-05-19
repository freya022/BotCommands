package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

interface IAggregatedParameter : MethodParameter {
    val aggregator: KFunction<*>
    /**
     * Nullable to accommodate for constructor aggregators (they are not tied to classes)
     */
    val aggregatorInstance: Any?
    val aggregatorHasEvent: Boolean

    val options: List<Option>

    val nestedAggregatedParameters: List<IAggregatedParameter>

    val allOptions: List<Option>
        get() = options + nestedAggregatedParameters.flatMap { it.allOptions }

    companion object {
        internal fun KFunction<*>.hasEvent() = this.nonInstanceParameters.first().type.jvmErasure.isSubclassOf(Event::class)
    }
}