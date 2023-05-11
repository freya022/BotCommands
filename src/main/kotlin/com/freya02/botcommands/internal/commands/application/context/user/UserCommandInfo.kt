package com.freya02.botcommands.internal.commands.application.context.user

import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.context.user.GlobalUserEvent
import com.freya02.botcommands.api.commands.application.context.user.GuildUserEvent
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedMethodParameter
import com.freya02.botcommands.internal.commands.application.context.user.mixins.ITopLevelUserCommandInfo
import com.freya02.botcommands.internal.commands.application.context.user.mixins.TopLevelUserCommandInfoMixin
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkEventScope
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import com.freya02.botcommands.internal.core.CooldownService
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.utils.InsertOptionResult
import com.freya02.botcommands.internal.utils.mapFinalParameters
import com.freya02.botcommands.internal.utils.mapOptions
import com.freya02.botcommands.internal.utils.tryInsertNullableOption
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.valueParameters

class UserCommandInfo internal constructor(
    private val context: BContextImpl,
    builder: UserCommandBuilder
) : ApplicationCommandInfo(context, builder),
    ITopLevelUserCommandInfo by TopLevelUserCommandInfoMixin(context, builder) {

    override val topLevelInstance: ITopLevelApplicationCommandInfo = this
    override val parentInstance = null
    override val parameters: List<UserContextCommandParameter>

    init {
        requireFirstParam(method.valueParameters, GlobalUserEvent::class)

        builder.checkEventScope<GuildUserEvent>()

        parameters = builder.optionAggregateBuilders.transform<UserCommandOptionAggregateBuilder, _> {
            UserContextCommandParameter(context, it)
        }
    }

    internal suspend fun execute(jdaEvent: UserContextInteractionEvent, cooldownService: CooldownService): Boolean {
        val event = when {
            isGuildOnly -> GuildUserEvent(context, jdaEvent)
            else -> GlobalUserEvent(context, jdaEvent)
        }

        val optionValues = parameters.mapOptions { option ->
            if (tryInsertOption(event, this, option) == InsertOptionResult.ABORT)
                return false
        }

        cooldownService.applyCooldown(this, event)

        method.callSuspendBy(parameters.mapFinalParameters(event, optionValues))

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

                option.resolver.resolveSuspend(context, this, event)
            }
            OptionType.CUSTOM -> {
                option as CustomMethodOption

                option.resolver.resolveSuspend(context, this, event)
            }
            OptionType.GENERATED -> {
                option as ApplicationGeneratedMethodParameter

                option.getCheckedDefaultValue { it.generatedValueSupplier.getDefaultValue(event) }
            }
            else -> throwInternal("MethodParameterType#${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, event, option, optionMap)
    }
}