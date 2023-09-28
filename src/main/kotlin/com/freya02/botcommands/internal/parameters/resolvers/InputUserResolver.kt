package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.entities.InputUser
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation
import com.freya02.botcommands.internal.commands.prefixed.TextUtils.findEntity
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.core.entities.InputUserImpl
import com.freya02.botcommands.internal.utils.throwInternal
import com.freya02.botcommands.internal.utils.throwUser
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping

@Resolver
internal class InputUserResolver internal constructor(): AbstractUserSnowflakeResolver<InputUserResolver, InputUser>(InputUser::class) {
    override suspend fun resolveSuspend(
        context: BContext,
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): InputUser? {
        val id = args[0]?.toLong() ?: throwInternal("Required pattern group is missing")
        return retrieveInputUserOrNull(id, event.message, ::InputUserImpl)
    }

    override fun resolve(
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): InputUser = InputUserImpl(optionMapping.asUser, optionMapping.asMember)

    override suspend fun resolveSuspend(
        context: BContext,
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): InputUser? {
        val id = arg.toLongOrNull() ?: throwUser("Invalid user id: $arg")
        return retrieveInputUserOrNull(id, event.message, ::InputUserImpl)
    }

    override fun resolve(context: BContext, info: UserCommandInfo, event: UserContextInteractionEvent): InputUser =
        InputUserImpl(event.target, event.targetMember)

    internal suspend fun <R : IMentionable> retrieveInputUserOrNull(userId: Long, message: Message, transform: (User, Member?) -> R): R? {
        val guild = if (message.isFromGuild) message.guild else null
        val memberResult = runCatching {
            if (guild == null)
                return@runCatching null
            message.mentions.members.findEntity(userId) { guild.retrieveMemberById(userId).await() }.let { transform(it.user, it) }
        }
        memberResult.getOrNull()?.let { return it }

        val userResult = runCatching {
            message.mentions.users.findEntity(userId) { message.jda.retrieveUserById(userId).await() }.let { transform(it, null) }
        }
        if (userResult.isSuccess) return userResult.getOrThrow()

        if (memberResult.isFailure) {
            LOGGER.trace { "Could not resolve input user in ${guild!!.name} (${guild.idLong}): ${memberResult.exceptionOrNull()!!.message} / ${userResult.exceptionOrNull()!!.message}" }
        }

        return null
    }
}