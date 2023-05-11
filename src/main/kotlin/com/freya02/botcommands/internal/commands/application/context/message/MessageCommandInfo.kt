package com.freya02.botcommands.internal.commands.application.context.message

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedMethodParameter
import com.freya02.botcommands.internal.commands.application.context.message.mixins.ITopLevelMessageCommandInfo
import com.freya02.botcommands.internal.commands.application.context.message.mixins.TopLevelMessageCommandInfoMixin
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
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import kotlin.collections.set
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
        requireFirstParam(method.valueParameters, GlobalMessageEvent::class)

        builder.checkEventScope<GuildMessageEvent>()

        parameters = builder.optionAggregateBuilders.transform<MessageCommandOptionAggregateBuilder, _> {
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

        method.callSuspendBy(parameters.mapFinalParameters(event, optionValues))

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
                option as ApplicationGeneratedMethodParameter

                option.getCheckedDefaultValue { it.generatedValueSupplier.getDefaultValue(event) }
            }
            else -> throwInternal("MethodParameterType#${option.optionType} has not been implemented")
        }

        if (value != null) {
            optionMap[option] = value

            return InsertOptionResult.OK
        } else {
            //TODO possibly refactor with other handlers, as they all use InsertOptionResult
            if (option.isVararg) {
                //Continue looking at other options
                return InsertOptionResult.SKIP
            } else if (option.isOptional) { //Default or nullable
                //Put null/default value if parameter is not a kotlin default value
                return if (option.kParameter.isOptional) {
                    InsertOptionResult.SKIP //Kotlin default value, don't add anything to the parameters map
                } else {
                    //Nullable
                    optionMap[option] = when {
                        option.isPrimitive -> 0
                        else -> null
                    }
                    InsertOptionResult.SKIP
                }
            } else {
                throwUser("Message command parameter couldn't be resolved at option ${option.declaredName}")
            }
        }
    }
}