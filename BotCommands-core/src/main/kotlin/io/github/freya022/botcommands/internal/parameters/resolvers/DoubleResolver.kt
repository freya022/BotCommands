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
class DoubleResolver : ClassParameterResolver<DoubleResolver, Double>(Double::class),
                       TextParameterResolver<DoubleResolver, Double>,
                       SlashParameterResolver<DoubleResolver, Double> {

    override val pattern: Pattern = Pattern.compile("([-+]?[0-9]*[.,]?[0-9]+)")
    override val testExample: String = "1234.42"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String = "3.14159"

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): Double? = args[0]!!.toDoubleOrNull()


    override val optionType: OptionType get() = OptionType.NUMBER

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Double? {
        return try {
            optionMapping.asDouble
        } catch (e: NumberFormatException) { //Can't have discord to send us actual input when autocompleting lmao
            0.0
        }
    }
}
