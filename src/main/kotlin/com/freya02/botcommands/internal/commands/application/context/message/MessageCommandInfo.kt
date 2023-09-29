package com.freya02.botcommands.internal.commands.application.context.message

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent
import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedOption
import com.freya02.botcommands.internal.commands.application.context.message.mixins.ITopLevelMessageCommandInfo
import com.freya02.botcommands.internal.commands.application.context.message.mixins.TopLevelMessageCommandInfoMixin
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.core.reflection.checkEventScope
import com.freya02.botcommands.internal.core.reflection.toMemberEventFunction
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.transform
import com.freya02.botcommands.internal.utils.*
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import kotlin.reflect.full.callSuspendBy

class MessageCommandInfo internal constructor(
    private val context: BContextImpl,
    builder: MessageCommandBuilder
) : ApplicationCommandInfo(builder),
    ITopLevelMessageCommandInfo by TopLevelMessageCommandInfoMixin(builder) {

    override val eventFunction = builder.toMemberEventFunction<GlobalMessageEvent, _>(context)

    override val topLevelInstance: ITopLevelApplicationCommandInfo = this
    override val parentInstance = null
    override val parameters: List<MessageContextCommandParameter>

    init {
        eventFunction.checkEventScope<GuildMessageEvent>(builder)

        parameters = builder.optionAggregateBuilders.transform {
            MessageContextCommandParameter(context, it)
        }
    }

    internal suspend fun execute(jdaEvent: MessageContextInteractionEvent, cancellableRateLimit: CancellableRateLimit): Boolean {
        val event = when {
            isGuildOnly -> GuildMessageEvent(context, jdaEvent, cancellableRateLimit)
            else -> GlobalMessageEvent(context, jdaEvent, cancellableRateLimit)
        }

        val optionValues = parameters.mapOptions { option ->
            if (tryInsertOption(event, this, option) == InsertOptionResult.ABORT)
                return false
        }

        val finalParameters = parameters.mapFinalParameters(event, optionValues)
        function.callSuspendBy(finalParameters)

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