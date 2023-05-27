package com.freya02.botcommands.internal.core.reflection

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.builder.ExecutableCommandBuilder
import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandVariationBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.isFakeSlashFunction
import com.freya02.botcommands.internal.core.ClassPathFunction
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSingleAggregator
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

sealed class Function<R>(kFunction: KFunction<R>) {
    val kFunction = kFunction.reflectReference()
    val parametersSize = kFunction.parameters.size

    override fun equals(other: Any?) = kFunction == other
    override fun hashCode() = kFunction.hashCode()
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

    val isSingleAggregator = this.kFunction.isSingleAggregator()

    internal constructor(context: BContextImpl, aggregator: KFunction<*>) : this(aggregator, context.serviceContainer.getFunctionServiceOrNull(aggregator))

    internal suspend fun aggregate(event: Event, aggregatorArguments: MutableMap<KParameter, Any?>): Any? {
        if (instanceParameter != null) {
            aggregatorArguments[instanceParameter] = aggregatorInstance
                ?: throwInternal(kFunction, "Aggregator's instance parameter was not retrieved but was necessary")
        }

        if (eventParameter != null) {
            aggregatorArguments[eventParameter] = event
        }

        return kFunction.callSuspendBy(aggregatorArguments)
    }
}

internal fun KFunction<*>.toAggregatorFunction(context: BContextImpl) = AggregatorFunction(context, this)

open class MemberFunction<R> private constructor(
    function: KFunction<R>,
    instanceSupplier: () -> Any,
    val firstParameter: KParameter
) : Function<R>(function) {
    val instance by lazy(instanceSupplier)

    val resolvableParameters = kFunction.valueParameters.drop(1) //Drop the first parameter
    val instanceParameter = kFunction.instanceParameter
        ?: throwInternal(kFunction, "Function shouldn't be static or constructors")

    internal constructor(function: KFunction<R>, instanceSupplier: () -> Any) : this(
        function = function,
        instanceSupplier = instanceSupplier,
        firstParameter = function.nonInstanceParameters.firstOrNull()
            ?: throwInternal(function, "The function should have been checked to have at least one parameter")
    )
}

internal fun ClassPathFunction.toMemberFunction() = MemberFunction(function, instanceSupplier = { this.instance })

class MemberEventFunction<T : Event, R> internal constructor(
    function: KFunction<R>,
    instanceSupplier: () -> Any,
    eventClass: KClass<T>
) : MemberFunction<R>(function, instanceSupplier) {
    val eventParameter get() = firstParameter

    init {
        requireUser(eventParameter.type.jvmErasure.isSubclassOf(eventClass), function) {
            "First argument should be a ${eventClass.simpleNestedName}"
        }
    }

    internal constructor(context: BContextImpl, function: KFunction<R>, eventClass: KClass<T>) : this(
        function = function,
        instanceSupplier = { context.serviceContainer.getFunctionService(function) },
        eventClass = eventClass
    )
}

// Using the builder to get the scope is required as the info object is still initializing
// and would NPE when getting the top level instance
internal inline fun <reified T> MemberEventFunction<out GenericCommandInteractionEvent, *>.checkEventScope(
    builder: ApplicationCommandBuilder<*>
) {
    if (kFunction.isFakeSlashFunction()) return

    val eventType = eventParameter.type.jvmErasure
    if (builder.topLevelBuilder.scope.isGuildOnly) {
        if (!eventType.isSubclassOf(T::class)) {
            Logging.getLogger().warn("${kFunction.shortSignature} : First parameter could be a ${T::class.simpleName} as to benefit from non-null getters")
        }
    } else if (eventType.isSubclassOf(T::class)) {
        throwUser("Cannot use ${T::class.simpleName} on a global application command")
    }
}

internal inline fun <reified T : Event> ClassPathFunction.toMemberEventFunction() =
    MemberEventFunction(function, instanceSupplier = { instance }, T::class)

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