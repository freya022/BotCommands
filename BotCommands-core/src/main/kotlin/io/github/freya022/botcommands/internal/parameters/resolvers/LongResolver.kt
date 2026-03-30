package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern

@Resolver
class LongResolver : ClassParameterResolver<LongResolver, Long>(Long::class),
                     TextParameterResolver<LongResolver, Long>,
                     SlashParameterResolver<LongResolver, Long> {

    override val pattern: Pattern = Pattern.compile("(\\d+)")
    override val testExample: String = "1234"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String =
        if (option.isId) "222046562543468545" else "42"

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): Long? = args[0]!!.toLongOrNull()


    override val optionType: OptionType
        get() = OptionType.INTEGER

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Long? {
        return try {
            optionMapping.asLong
        } catch (e: NumberFormatException) { //Can't have discord to send us actual input when autocompleting lmao
            0L
        }
    }
}
