package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandOption
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern

@Resolver
internal object UserSnowflakeResolver :
        ClassParameterResolver<UserSnowflakeResolver, UserSnowflake>(UserSnowflake::class),
        TextParameterResolver<UserSnowflakeResolver, UserSnowflake>,
        SlashParameterResolver<UserSnowflakeResolver, UserSnowflake>,
        ComponentParameterResolver<UserSnowflakeResolver, UserSnowflake>,
        UserContextParameterResolver<UserSnowflakeResolver, UserSnowflake> {

    override val optionType: OptionType = OptionType.USER
    override val pattern: Pattern get() = AbstractUserSnowflakeResolver.userMentionPattern
    override val testExample: String = "<@1234>"

    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String {
        return event.member.asMention
    }

    override suspend fun resolveSuspend(event: GenericComponentInteractionCreateEvent, arg: String): UserSnowflake =
        UserSnowflake.fromId(arg)

    override suspend fun resolveSuspend(
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping,
    ): UserSnowflake = optionMapping.asUser

    override suspend fun resolveSuspend(
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): UserSnowflake = UserSnowflake.fromId(args[0]!!)

    override suspend fun resolveSuspend(info: UserCommandInfo, event: UserContextInteractionEvent): UserSnowflake =
        event.target
}