package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.parameters.*
import com.freya02.botcommands.api.parameters.ParameterType.Companion.ofKClass
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.modals.ModalHandlerInfo
import com.freya02.botcommands.internal.prefixed.TextCommandInfo
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import java.util.regex.Pattern

class StringResolver : ParameterResolver(ofKClass(String::class)), RegexParameterResolver,
    QuotableRegexParameterResolver,
    SlashParameterResolver, ComponentParameterResolver, ModalParameterResolver {
    override fun resolve(
        context: BContext,
        info: TextCommandInfo,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): Any? {
        return args[0]
    }

    override fun getPattern(): Pattern {
        return Pattern.compile("(\\X+)")
    }

    override fun getQuotedPattern(): Pattern {
        return Pattern.compile("\"(\\X+)\"")
    }

    override fun getTestExample(): String {
        return "foobar"
    }

    override fun getOptionType(): OptionType {
        return OptionType.STRING
    }

    override fun resolve(
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Any {
        return optionMapping.asString
    }

    override fun resolve(
        context: BContext,
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): Any {
        return arg
    }

    override fun resolve(
        context: BContext,
        info: ModalHandlerInfo,
        event: ModalInteractionEvent,
        modalMapping: ModalMapping
    ): Any {
        return modalMapping.asString
    }
}