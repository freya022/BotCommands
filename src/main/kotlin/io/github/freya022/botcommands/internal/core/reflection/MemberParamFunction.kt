package io.github.freya022.botcommands.internal.core.reflection

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.builder.ExecutableCommandBuilder
import io.github.freya022.botcommands.api.commands.builder.IBuilderFunctionHolder
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Logging
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.isFakeSlashFunction
import io.github.freya022.botcommands.internal.core.ClassPathFunction
import io.github.freya022.botcommands.internal.core.service.getFunctionService
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonEventParameters
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

internal class MemberParamFunction<T : Any, R> internal constructor(
    boundFunction: KFunction<R>,
    instanceSupplier: () -> Any,
    paramClass: KClass<T>
) : MemberFunction<R>(boundFunction, instanceSupplier) {
    init {
        requireAt(firstParameter.type.jvmErasure.isSubclassOf(paramClass), kFunction) {
            "First argument should be a ${paramClass.simpleNestedName}"
        }
    }

    internal constructor(context: BContext, boundFunction: KFunction<R>, paramClass: KClass<T>) : this(
        boundFunction = boundFunction,
        instanceSupplier = { context.serviceContainer.getFunctionService(boundFunction) },
        paramClass = paramClass
    )
}

// Using the builder to get the scope is required as the info object is still initializing
// and would NPE when getting the top level instance
internal inline fun <reified GUILD_T : GenericCommandInteractionEvent> MemberParamFunction<out GenericCommandInteractionEvent, *>.checkEventScope(
    builder: ApplicationCommandBuilder<*>
) {
    if (kFunction.isFakeSlashFunction()) return

    val eventType = firstParameter.type.jvmErasure
    if (builder.topLevelBuilder.scope.isGuildOnly) {
        if (!eventType.isSubclassOf<GUILD_T>()) {
            // Do not warn about guild-restricted types when everything is forced as a guild command
            if (builder.context.applicationConfig.forceGuildCommands) return

            Logging.getLogger().warn("${kFunction.shortSignature} : First parameter could be a ${classRef<GUILD_T>()} as to benefit from non-null getters")
        }
    } else if (eventType.isSubclassOf<GUILD_T>()) {
        throwArgument(kFunction, "Cannot use ${classRef<GUILD_T>()} on a global application command")
    }
}

internal inline fun <reified T : Any> ClassPathFunction.toMemberParamFunction() =
    MemberParamFunction(function, instanceSupplier = { instance }, T::class)

internal inline fun <reified T : Any, R> KFunction<R>.toMemberParamFunction(context: BContext) =
    MemberParamFunction(context, this, T::class)

internal inline fun <reified T : Any, R> IBuilderFunctionHolder<R>.toMemberParamFunction(context: BContext): MemberParamFunction<T, R> {
    if (this is ExecutableCommandBuilder<*, *>) {
        requireAt(function.nonEventParameters.size == optionAggregateBuilders.size, function) {
            "Function must have the same number of options declared as on the method"
        }
    } else if (this is TextCommandVariationBuilder) {
        requireAt(function.nonEventParameters.size == optionAggregateBuilders.size, function) {
            "Function must have the same number of options declared as on the method"
        }
    }

    return MemberParamFunction(context, this.function, T::class)
}