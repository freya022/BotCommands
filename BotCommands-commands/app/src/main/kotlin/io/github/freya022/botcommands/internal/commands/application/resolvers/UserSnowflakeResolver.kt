package io.github.freya022.botcommands.internal.commands.application.resolvers

import io.github.freya022.botcommands.api.commands.application.context.user.options.UserContextCommandOption
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType

@Resolver
internal object UserSnowflakeResolver :
        ClassParameterResolver<UserSnowflakeResolver, UserSnowflake>(UserSnowflake::class),
        SlashParameterResolver<UserSnowflakeResolver, UserSnowflake>,
        UserContextParameterResolver<UserSnowflakeResolver, UserSnowflake> {

    override val optionType: OptionType = OptionType.USER

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping,
    ): UserSnowflake = optionMapping.asUser

    override suspend fun resolveSuspend(option: UserContextCommandOption, event: UserContextInteractionEvent): UserSnowflake =
        event.target
}
