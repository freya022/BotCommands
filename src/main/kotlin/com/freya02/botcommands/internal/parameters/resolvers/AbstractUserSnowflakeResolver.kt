package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.core.BContext
import com.freya02.botcommands.api.parameters.*
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation
import com.freya02.botcommands.internal.commands.prefixed.TextUtils.findEntity
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.utils.throwInternal
import com.freya02.botcommands.internal.utils.throwUser
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

internal sealed class AbstractUserSnowflakeResolver<T : AbstractUserSnowflakeResolver<T, R>, R : UserSnowflake>(
    clazz: KClass<R>
) : ParameterResolver<T, R>(clazz),
    RegexParameterResolver<T, R>,
    SlashParameterResolver<T, R>,
    ComponentParameterResolver<T, R>,
    UserContextParameterResolver<T, R> {

    final override val pattern: Pattern = Pattern.compile("(?:<@!?)?(\\d+)>?")
    final override val testExample: String = "<@1234>"

    final override fun getHelpExample(parameter: KParameter, event: BaseCommandEvent, isID: Boolean): String {
        return event.member.asMention
    }

    final override val optionType: OptionType = OptionType.USER

    final override suspend fun resolveSuspend(
        context: BContext,
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): R? {
        val id = args[0]?.toLong() ?: throwInternal("Required pattern group is missing")
        return retrieveOrNull(id, event.message)
    }

    final override fun resolve(
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): R? = transformEntities(optionMapping.asUser, optionMapping.asMember)

    final override suspend fun resolveSuspend(
        context: BContext,
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): R? {
        val id = arg.toLongOrNull() ?: throwUser("Invalid user id: $arg")
        return retrieveOrNull(id, event.message)
    }

    final override fun resolve(context: BContext, info: UserCommandInfo, event: UserContextInteractionEvent): R? =
        transformEntities(event.target, event.targetMember)

    private suspend fun retrieveOrNull(userId: Long, message: Message): R? {
        val guild = if (message.isFromGuild) message.guild else null
        val memberResult = runCatching {
            if (guild == null)
                return@runCatching null
            message.mentions.members.findEntity(userId) { guild.retrieveMemberById(userId).await() }.let { transformEntities(it.user, it) }
        }
        memberResult.getOrNull()?.let { return it }

        val userResult = runCatching {
            message.mentions.users.findEntity(userId) { message.jda.retrieveUserById(userId).await() }.let { transformEntities(it, null) }
        }
        if (userResult.isSuccess) return userResult.getOrThrow()

        if (memberResult.isFailure) {
            LOGGER.trace { "Could not resolve input user in ${guild!!.name} (${guild.idLong}): ${memberResult.exceptionOrNull()!!.message} / ${userResult.exceptionOrNull()!!.message}" }
        }

        return null
    }

    protected abstract fun transformEntities(user: User, member: Member?): R?
}