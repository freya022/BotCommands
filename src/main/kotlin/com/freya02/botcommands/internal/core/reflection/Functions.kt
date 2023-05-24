package com.freya02.botcommands.internal.core.reflection

import com.freya02.botcommands.api.commands.builder.ExecutableCommandBuilder
import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandVariationBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KClass
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

class MemberEventFunction<T : Event, R> private constructor(
    function: KFunction<R>,
    val instance: Any,
    val eventParameter: KParameter
) : Function<R>(function) {
    val instanceParameter = this.kFunction.instanceParameter
        ?: throwInternal("Commands shouldn't be static or constructors")

    internal constructor(function: KFunction<R>, instance: Any, eventClass: KClass<T>) : this(
        function = function,
        instance = instance,
        eventParameter = function.nonInstanceParameters.firstOrNull { it.type.jvmErasure.isSubclassOf(eventClass) }
            ?: throwUser("First argument should be a ${eventClass.simpleNestedName}")
    )

    internal constructor(context: BContextImpl, function: KFunction<R>, eventClass: KClass<T>) : this(
        function = function,
        instance = context.serviceContainer.getFunctionService(function),
        eventClass = eventClass
    )
}

internal inline fun <reified T : Event> ClassPathFunction.toMemberEventFunction() =
    MemberEventFunction(function, instance, T::class)

internal inline fun <reified T : Event, R> KFunction<R>.toMemberEventFunction(context: BContextImpl) =
    MemberEventFunction(context, this, T::class)

internal inline fun <reified T : Event, R> IBuilderFunctionHolder<R>.toMemberEventFunction(context: BContextImpl): MemberEventFunction<T, R> {
    if (this is ExecutableCommandBuilder<*, *>) {
        requireUser(function.nonEventParameters.size == optionAggregateBuilders.size, function) {
            "Function must have the same number of options declared as on the method"
        }
    } else if (this is TextCommandVariationBuilder) {
        requireUser(function.nonEventParameters.size == optionAggregateBuilders.size, function) {
            "Function must have the same number of options declared as on the method"
        }
    }

    return MemberEventFunction(context, this.function, T::class)
}