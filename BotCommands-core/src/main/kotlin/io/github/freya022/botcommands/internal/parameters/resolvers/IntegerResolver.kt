package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@Resolver
class IntegerResolver : ClassParameterResolver<IntegerResolver, Int>(Int::class),
                        SlashParameterResolver<IntegerResolver, Int> {

    override val optionType: OptionType = OptionType.INTEGER

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Int? {
        return try {
            optionMapping.asInt
        } catch (e: NumberFormatException) { //Can't have discord to send us actual input when autocompleting lmao
            0
        }
    }
}
