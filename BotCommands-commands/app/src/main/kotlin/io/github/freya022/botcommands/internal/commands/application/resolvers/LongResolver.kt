package io.github.freya022.botcommands.internal.commands.application.resolvers

import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@Resolver
internal class LongResolver : ClassParameterResolver<LongResolver, Long>(Long::class),
                              SlashParameterResolver<LongResolver, Long> {

    override val optionType: OptionType
        get() = OptionType.INTEGER

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping,
    ): Long? {
        return try {
            optionMapping.asLong
        } catch (e: NumberFormatException) { //Can't have discord to send us actual input when autocompleting lmao
            0L
        }
    }
}
