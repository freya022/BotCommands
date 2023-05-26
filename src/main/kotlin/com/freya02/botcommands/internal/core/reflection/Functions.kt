package com.freya02.botcommands.internal.core.reflection

import com.freya02.botcommands.api.commands.builder.ExecutableCommandBuilder
import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandVariationBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSingleAggregator
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSpecialAggregator
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isVarargAggregator
import com.freya02.botcommands.internal.utils.ReflectionUtils.collectionElementType
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import net.dv8tion.jda.api.events.Event
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

open class Function<R> internal constructor(kFunction: KFunction<R>) {
    val kFunction = kFunction.reflectReference()

    override fun equals(other: Any?) = kFunction == other
    override fun hashCode() = kFunction.hashCode()
}

internal inline fun <reified T : Annotation> Function<*>.findAnnotation() = kFunction.findAnnotation<T>()

internal val Function<*>.parameters get() = kFunction.parameters
internal val Function<*>.nonInstanceParameters get() = kFunction.nonInstanceParameters

internal val Function<*>.collectionElementType get() = kFunction.collectionElementType
internal val Function<*>.shortSignature get() = kFunction.shortSignature
internal val Function<*>.shortSignatureNoSrc get() = kFunction.shortSignatureNoSrc

internal fun Function<*>.findParameterByName(name: String) = kFunction.findParameterByName(name)

internal suspend fun <R> Function<R>.callSuspendBy(args: Map<KParameter, Any?>) = kFunction.callSuspendBy(args)

internal fun Function<*>.throwUser(message: String): Nothing = throwUser(kFunction, message)
internal fun Function<*>.throwInternal(message: String): Nothing = throwInternal(kFunction, message)

internal fun Function<*>.rethrowUser(message: String, e: Throwable): Nothing =
    throw RuntimeException("$shortSignature : $message", e)

@OptIn(ExperimentalContracts::class)
internal inline fun Function<*>.requireUser(value: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        throwUser(lazyMessage())
    }
}

class AggregatorFunction private constructor(
    aggregator: KFunction<*>,
    /**
     * Nullable due to constructor aggregators
     */
    private val aggregatorInstance: Any?
) : Function<Any?>(aggregator) {
    private val instanceParameter = this.kFunction.instanceParameter
    private val eventParameter = this.kFunction.nonInstanceParameters.firstOrNull { it.type.jvmErasure.isSubclassOf(Event::class) }

    internal constructor(context: BContextImpl, aggregator: KFunction<*>) : this(aggregator, context.serviceContainer.getFunctionServiceOrNull(aggregator))

    internal suspend fun aggregate(event: Event, aggregatorArguments: MutableMap<KParameter, Any?>): Any? {
        if (instanceParameter != null) {
            aggregatorArguments[instanceParameter] = aggregatorInstance
                ?: throwInternal("Aggregator's instance parameter was not retrieved but was necessary")
        }

        if (eventParameter != null) {
            aggregatorArguments[eventParameter] = event
        }

        return callSuspendBy(aggregatorArguments)
    }
}

fun AggregatorFunction.isSingleAggregator() = this.kFunction.isSingleAggregator()
fun AggregatorFunction.isVarargAggregator() = this.kFunction.isVarargAggregator()
fun AggregatorFunction.isSpecialAggregator() = this.kFunction.isSpecialAggregator()

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
            ?: throwUser(function, "First argument should be a ${eventClass.simpleNestedName}")
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