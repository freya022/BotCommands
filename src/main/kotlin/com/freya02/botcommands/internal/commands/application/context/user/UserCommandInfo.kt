package com.freya02.botcommands.internal.commands.application.context.user

import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.context.user.GlobalUserEvent
import com.freya02.botcommands.api.commands.application.context.user.GuildUserEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedOption
import com.freya02.botcommands.internal.commands.application.context.user.mixins.ITopLevelUserCommandInfo
import com.freya02.botcommands.internal.commands.application.context.user.mixins.TopLevelUserCommandInfoMixin
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import com.freya02.botcommands.internal.core.CooldownService
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.core.reflection.checkEventScope
import com.freya02.botcommands.internal.core.reflection.toMemberEventFunction
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.transform
import com.freya02.botcommands.internal.utils.*
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import kotlin.reflect.full.callSuspendBy

class UserCommandInfo internal constructor(
    private val context: BContextImpl,
    builder: UserCommandBuilder
) : ApplicationCommandInfo(builder),
    ITopLevelUserCommandInfo by TopLevelUserCommandInfoMixin(builder) {

    override val eventFunction = builder.toMemberEventFunction<GlobalUserEvent, _>(context)

    override val topLevelInstance: ITopLevelApplicationCommandInfo = this
    override val parentInstance = null
    override val parameters: List<UserContextCommandParameter>

    init {
        eventFunction.checkEventScope<GuildUserEvent>(builder)

        parameters = builder.optionAggregateBuilders.transform {
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

        function.callSuspendBy(parameters.mapFinalParameters(event, optionValues))

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
                option as ApplicationGeneratedOption

                option.getCheckedDefaultValue { it.generatedValueSupplier.getDefaultValue(event) }
            }
            else -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}