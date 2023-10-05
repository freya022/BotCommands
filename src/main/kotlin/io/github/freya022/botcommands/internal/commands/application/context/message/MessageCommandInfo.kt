package io.github.freya022.botcommands.internal.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.application.ApplicationGeneratedOption
import io.github.freya022.botcommands.internal.commands.application.context.message.mixins.ITopLevelMessageCommandInfo
import io.github.freya022.botcommands.internal.commands.application.context.message.mixins.TopLevelMessageCommandInfoMixin
import io.github.freya022.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import io.github.freya022.botcommands.internal.core.options.Option
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.checkEventScope
import io.github.freya022.botcommands.internal.core.reflection.toMemberEventFunction
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.transform
import io.github.freya022.botcommands.internal.utils.*
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import kotlin.reflect.full.callSuspendBy

class MessageCommandInfo internal constructor(
    private val context: BContext,
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
            else -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}