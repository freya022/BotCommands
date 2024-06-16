package io.github.freya022.botcommands.internal.parameters.resolvers

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandOption
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.commands.text.TextUtils.findEntity
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
import io.github.oshai.kotlinlogging.KotlinLogging
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

private val logger = KotlinLogging.logger { }

internal sealed class AbstractUserSnowflakeResolver<T : AbstractUserSnowflakeResolver<T, R>, R : UserSnowflake>(
    protected val context: BContext,
    clazz: KClass<R>
) : ClassParameterResolver<T, R>(clazz),
    TextParameterResolver<T, R>,
    SlashParameterResolver<T, R>,
    ComponentParameterResolver<T, R>,
    UserContextParameterResolver<T, R> {

    final override val pattern: Pattern = Pattern.compile("(?:<@!?)?(\\d+)>?")
    final override val testExample: String = "<@1234>"

    final override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String {
        return event.member.asMention
    }

    final override val optionType: OptionType = OptionType.USER

    final override suspend fun resolveSuspend(
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): R? {
        val id = args[0]?.toLong() ?: throwInternal("Required pattern group is missing")
        return retrieveOrNull(id, event.message)
    }

    final override fun resolve(
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): R? = transformEntities(optionMapping.asUser, optionMapping.asMember)

    final override suspend fun resolveSuspend(event: GenericComponentInteractionCreateEvent, arg: String): R? {
        val id = arg.toLongOrNull() ?: throwUser("Invalid user id: $arg")
        val entity = retrieveOrNull(id, event.message)
        if (entity == null)
            event.reply_(context.getDefaultMessages(event).resolverUserNotFoundMsg, ephemeral = true).queue()

        return entity
    }

    final override fun resolve(info: UserCommandInfo, event: UserContextInteractionEvent): R? =
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
            logger.trace { "Could not resolve input user in ${guild!!.name} (${guild.idLong}): ${memberResult.exceptionOrNull()!!.message} / ${userResult.exceptionOrNull()!!.message}" }
        }

        return null
    }

    protected abstract fun transformEntities(user: User, member: Member?): R?
}