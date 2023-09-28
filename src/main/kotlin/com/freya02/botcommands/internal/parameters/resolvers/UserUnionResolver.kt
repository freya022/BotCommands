package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.core.entities.UserUnion
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.parameters.*
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation
import com.freya02.botcommands.internal.commands.prefixed.TextUtils.findEntity
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.core.entities.UserUnionImpl
import com.freya02.botcommands.internal.utils.throwInternal
import com.freya02.botcommands.internal.utils.throwUser
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern
import kotlin.reflect.KParameter

@Resolver
internal class UserUnionResolver internal constructor(): ParameterResolver<UserUnionResolver, UserUnion>(UserUnion::class),
    RegexParameterResolver<UserUnionResolver, UserUnion>,
    SlashParameterResolver<UserUnionResolver, UserUnion>,
    ComponentParameterResolver<UserUnionResolver, UserUnion>,
    UserContextParameterResolver<UserUnionResolver, UserUnion> {

    override val pattern: Pattern = Pattern.compile("(?:<@!?)?(\\d+)>?")
    override val testExample: String = "<@1234>"

    override fun getHelpExample(parameter: KParameter, event: BaseCommandEvent, isID: Boolean): String {
        return event.member.asMention
    }

    override val optionType: OptionType = OptionType.USER

    override suspend fun resolveSuspend(
        context: BContext,
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): UserUnion? {
        val id = args[0]?.toLong() ?: throwInternal("Required pattern group is missing")
        return retrieveUserUnionOrNull(id, event.message)
    }

    override fun resolve(
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): UserUnion = UserUnionImpl(optionMapping.asUser, optionMapping.asMember)

    override suspend fun resolveSuspend(
        context: BContext,
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): UserUnion? {
        val id = arg.toLongOrNull() ?: throwUser("Invalid user id: $arg")
        return retrieveUserUnionOrNull(id, event.message)
    }

    override fun resolve(context: BContext, info: UserCommandInfo, event: UserContextInteractionEvent): UserUnion =
        UserUnionImpl(event.target, event.targetMember)

    private suspend fun retrieveUserUnionOrNull(
        userId: Long,
        message: Message
    ): UserUnion? {
        val guild = if (message.isFromGuild) message.guild else null
        val memberResult = runCatching {
            if (guild == null)
                return@runCatching null
            message.mentions.members.findEntity(userId) { guild.retrieveMemberById(userId).await() }.let(::UserUnionImpl)
        }
        memberResult.getOrNull()?.let { return it }

        val userResult = runCatching {
            message.mentions.users.findEntity(userId) { message.jda.retrieveUserById(userId).await() }.let(::UserUnionImpl)
        }
        if (userResult.isSuccess) return userResult.getOrThrow()

        memberResult.onFailure {
            LOGGER.debug(
                "Could not resolve member of user union in {} ({}): {} (regex command, may not be an error)",
                guild!!.name,
                guild.idLong,
                it.message
            )
        }

        userResult.onFailure {
            LOGGER.debug(
                "Could not resolve user of user union: {} (regex command, may not be an error)",
                it.message
            )
        }

        return null
    }
}