package com.freya02.botcommands.internal.commands.application.context.message

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedOption
import com.freya02.botcommands.internal.commands.application.context.message.mixins.ITopLevelMessageCommandInfo
import com.freya02.botcommands.internal.commands.application.context.message.mixins.TopLevelMessageCommandInfoMixin
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkEventScope
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import com.freya02.botcommands.internal.core.CooldownService
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.requireFirstParam
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.transform
import com.freya02.botcommands.internal.utils.InsertOptionResult
import com.freya02.botcommands.internal.utils.mapFinalParameters
import com.freya02.botcommands.internal.utils.mapOptions
import com.freya02.botcommands.internal.utils.tryInsertNullableOption
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.valueParameters

class MessageCommandInfo internal constructor(
    private val context: BContextImpl,
    builder: MessageCommandBuilder
) : ApplicationCommandInfo(context, builder),
    ITopLevelMessageCommandInfo by TopLevelMessageCommandInfoMixin(context, builder) {

    override val topLevelInstance: ITopLevelApplicationCommandInfo = this
    override val parentInstance = null
    override val parameters: List<MessageContextCommandParameter>

    init {
        requireFirstParam(function.valueParameters, GlobalMessageEvent::class)

        builder.checkEventScope<GuildMessageEvent>()

        parameters = builder.optionAggregateBuilders.transform {
            MessageContextCommandParameter(context, it)
        }
    }

    internal suspend fun execute(jdaEvent: MessageContextInteractionEvent, cooldownService: CooldownService): Boolean {
        val event = when {
            isGuildOnly -> GuildMessageEvent(context, jdaEvent)
            else -> GlobalMessageEvent(context, jdaEvent)
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
        event: GlobalMessageEvent,
        optionMap: MutableMap<Option, Any?>,
        option: Option
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as MessageContextCommandOption

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