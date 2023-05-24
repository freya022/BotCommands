package com.freya02.botcommands.internal.core.reflection

import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

open class Function<R> internal constructor(val kFunction: KFunction<R>) : KFunction<R> by kFunction

class AggregatorFunction private constructor(
    aggregator: KFunction<*>,
    /**
     * Nullable due to constructor aggregators
     */
    private val aggregatorInstance: Any?
) : Function<Any?>(aggregator) {
    private val instanceParameter = this.kFunction.instanceParameter
    private val eventParameter = this.nonInstanceParameters.firstOrNull { it.type.jvmErasure.isSubclassOf(Event::class) }

    internal constructor(context: BContextImpl, aggregator: KFunction<*>) : this(aggregator, context.serviceContainer.getFunctionServiceOrNull(aggregator))

    internal suspend fun aggregate(event: Event, aggregatorArguments: MutableMap<KParameter, Any?>): Any? {
        if (instanceParameter != null) {
            aggregatorArguments[instanceParameter] = aggregatorInstance
                ?: throwInternal(this, "Aggregator's instance parameter was not retrieved but was necessary")
        }

        if (eventParameter != null) {
            aggregatorArguments[eventParameter] = event
        }

        return callSuspendBy(aggregatorArguments)
    }
}

internal fun KFunction<*>.toAggregatorFunction(context: BContextImpl) = AggregatorFunction(context, this)