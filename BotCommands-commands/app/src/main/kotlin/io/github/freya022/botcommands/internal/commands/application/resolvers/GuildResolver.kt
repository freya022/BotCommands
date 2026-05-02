package io.github.freya022.botcommands.internal.commands.application.resolvers

import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@Resolver
internal class GuildResolver : ClassParameterResolver<GuildResolver, Guild>(Guild::class),
                               SlashParameterResolver<GuildResolver, Guild> {

    override val optionType: OptionType = OptionType.STRING

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping,
    ): Guild? = event.jda.getGuildById(optionMapping.asString)
}
