package io.github.freya022.botcommands.internal.core.reflection

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.isAssignableFrom
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSingleAggregator
import io.github.freya022.botcommands.internal.core.service.getFunctionServiceOrNull
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.jvmErasure

internal class AggregatorFunction private constructor(
    boundAggregator: KFunction<*>,
    /**
     * Nullable due to constructor aggregators
     */
    private val aggregatorInstance: Any?,
    firstParamType: KClass<*>
) : Function<Any?>(boundAggregator) {
    private val instanceParameter = aggregator.instanceParameter
    private val eventParameter = aggregator.nonInstanceParameters.first().takeIf { it.type.jvmErasure.isAssignableFrom(firstParamType) }

    internal val aggregator get() = this.kFunction

    internal val isSingleAggregator get() = aggregator.isSingleAggregator()

    internal constructor(
        context: BContext,
        aggregator: KFunction<*>,
        firstParamType: KClass<*>
    ) : this(aggregator, context.serviceContainer.getFunctionServiceOrNull(aggregator), firstParamType)

    internal suspend fun aggregate(firstParam: Any, aggregatorArguments: MutableMap<KParameter, Any?>): Any? {
        if (instanceParameter != null) {
            aggregatorArguments[instanceParameter] = aggregatorInstance
                ?: throwInternal(aggregator, "Aggregator's instance parameter (${instanceParameter.type.jvmErasure.simpleNestedName}) was not retrieved but was necessary")
        }

        if (eventParameter != null) {
            aggregatorArguments[eventParameter] = firstParam
        }

        return aggregator.callSuspendBy(aggregatorArguments)
    }
}

internal fun KFunction<*>.toEventAggregatorFunction(context: BContext) =
    AggregatorFunction(context, this, Event::class)

internal fun KFunction<*>.toAggregatorFunction(context: BContext, firstParamType: KClass<*>) =
    AggregatorFunction(context, this, firstParamType)