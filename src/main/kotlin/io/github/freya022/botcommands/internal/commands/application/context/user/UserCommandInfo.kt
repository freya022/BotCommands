package io.github.freya022.botcommands.internal.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.application.ApplicationGeneratedOption
import io.github.freya022.botcommands.internal.commands.application.context.user.mixins.ITopLevelUserCommandInfo
import io.github.freya022.botcommands.internal.commands.application.context.user.mixins.TopLevelUserCommandInfoMixin
import io.github.freya022.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import io.github.freya022.botcommands.internal.core.options.Option
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.checkEventScope
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.parameters.ServiceMethodOption
import io.github.freya022.botcommands.internal.transform
import io.github.freya022.botcommands.internal.utils.*
import kotlin.reflect.full.callSuspendBy

class UserCommandInfo internal constructor(
    private val context: BContext,
    builder: UserCommandBuilder
) : ApplicationCommandInfo(builder),
    ITopLevelUserCommandInfo by TopLevelUserCommandInfoMixin(builder) {

    override val eventFunction = builder.toMemberParamFunction<GlobalUserEvent, _>(context)

    override val topLevelInstance: ITopLevelApplicationCommandInfo get() = this
    override val parentInstance get() = null
    override val parameters: List<UserContextCommandParameter>

    init {
        eventFunction.checkEventScope<GuildUserEvent>(builder)

        parameters = builder.optionAggregateBuilders.transform {
            UserContextCommandParameter(context, this, it)
        }
    }

    internal suspend fun execute(event: GlobalUserEvent): Boolean {
        val optionValues = parameters.mapOptions { option ->
            if (tryInsertOption(event, this, option) == InsertOptionResult.ABORT)
                return false
        }

        val finalParameters = parameters.mapFinalParameters(event, optionValues)
        function.callSuspendBy(finalParameters)

        return true
    }

    private suspend fun tryInsertOption(
        event: GlobalUserEvent,
        optionMap: MutableMap<Option, Any?>,
        option: Option
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as UserContextCommandOption

                option.resolver.resolveSuspend(this, event)
            }
            OptionType.CUSTOM -> {
                option as CustomMethodOption

                option.resolver.resolveSuspend(this, event)
            }
            OptionType.GENERATED -> {
                option as ApplicationGeneratedOption

                option.getCheckedDefaultValue { it.generatedValueSupplier.getDefaultValue(event) }
            }
            OptionType.SERVICE -> (option as ServiceMethodOption).getService()
            OptionType.CONSTANT -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}