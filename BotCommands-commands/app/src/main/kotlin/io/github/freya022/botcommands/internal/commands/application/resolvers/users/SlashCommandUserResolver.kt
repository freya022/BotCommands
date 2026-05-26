package io.github.freya022.botcommands.internal.commands.application.resolvers.users

import io.github.freya022.botcommands.api.commands.application.annotations.RequiresApplicationCommands
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@Resolver
@RequiresApplicationCommands
internal class SlashCommandUserResolver(
    private val resolver: SlashCommandInputUserResolver,
) : ClassParameterResolver<SlashCommandUserResolver, User>(User::class),
    SlashParameterResolver<SlashCommandUserResolver, User> {

    override val optionType: OptionType = resolver.optionType

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping,
    ): User {
        return resolver.resolve(option, event, optionMapping)
    }
}
