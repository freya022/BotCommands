package io.github.freya022.botcommands.internal.core.reflection

import io.github.freya022.botcommands.api.Logging
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.builder.ExecutableCommandBuilder
import io.github.freya022.botcommands.api.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.api.commands.prefixed.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.isFakeSlashFunction
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.shortSignature
import io.github.freya022.botcommands.internal.utils.requireUser
import io.github.freya022.botcommands.internal.utils.throwUser
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

class MemberEventFunction<T : Event, R> internal constructor(
    boundFunction: KFunction<R>,
    instanceSupplier: () -> Any,
    eventClass: KClass<T>
) : MemberFunction<R>(boundFunction, instanceSupplier) {
    val eventParameter get() = firstParameter

    init {
        requireUser(eventParameter.type.jvmErasure.isSubclassOf(eventClass), kFunction) {
            "First argument should be a ${eventClass.simpleNestedName}"
        }
    }

    internal constructor(context: BContextImpl, boundFunction: KFunction<R>, eventClass: KClass<T>) : this(
        boundFunction = boundFunction,
        instanceSupplier = { context.serviceContainer.getFunctionService(boundFunction) },
        eventClass = eventClass
    )
}

// Using the builder to get the scope is required as the info object is still initializing
// and would NPE when getting the top level instance
internal inline fun <reified GUILD_T : GenericCommandInteractionEvent> MemberEventFunction<out GenericCommandInteractionEvent, *>.checkEventScope(
    builder: ApplicationCommandBuilder<*>
) {
    if (kFunction.isFakeSlashFunction()) return

    val eventType = eventParameter.type.jvmErasure
    if (builder.topLevelBuilder.scope.isGuildOnly) {
        if (!eventType.isSubclassOf(GUILD_T::class)) {
            Logging.getLogger().warn("${kFunction.shortSignature} : First parameter could be a ${GUILD_T::class.simpleName} as to benefit from non-null getters")
        }
    } else if (eventType.isSubclassOf(GUILD_T::class)) {
        throwUser(kFunction, "Cannot use ${GUILD_T::class.simpleName} on a global application command")
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