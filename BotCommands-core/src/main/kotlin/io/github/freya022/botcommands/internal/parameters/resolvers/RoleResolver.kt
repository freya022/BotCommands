package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * @see RoleResolverFactoryProvider
 */
internal class RoleResolver : ClassParameterResolver<RoleResolver, Role>(Role::class),
                              SlashParameterResolver<RoleResolver, Role> {

    override val optionType: OptionType get() = OptionType.ROLE

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Role = optionMapping.asRole
}
