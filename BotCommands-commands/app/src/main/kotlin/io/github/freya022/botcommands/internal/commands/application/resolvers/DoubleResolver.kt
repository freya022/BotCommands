package io.github.freya022.botcommands.internal.commands.application.resolvers

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@Resolver
@RequiresApplicationCommands
internal class DoubleResolver : ClassParameterResolver<DoubleResolver, Double>(Double::class),
                                SlashParameterResolver<DoubleResolver, Double> {

    override val optionType: OptionType get() = OptionType.NUMBER

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping,
    ): Double? {
        return try {
            optionMapping.asDouble
        } catch (e: NumberFormatException) { //Can't have discord to send us actual input when autocompleting lmao
            0.0
        }
    }
}
