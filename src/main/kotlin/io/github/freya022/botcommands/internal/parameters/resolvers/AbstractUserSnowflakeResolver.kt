package io.github.freya022.botcommands.internal.parameters.resolvers

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.traceNull
import io.github.freya022.botcommands.api.core.utils.retrieveMemberOrNull
import io.github.freya022.botcommands.api.core.utils.retrieveUserOrNull
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.utils.ifNullThrowInternal
import io.github.freya022.botcommands.internal.utils.throwArgument
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.*
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
    context: BContext,
    clazz: KClass<R>
) : ClassParameterResolver<T, R>(clazz),
    TextParameterResolver<T, R>,
    SlashParameterResolver<T, R>,
    ComponentParameterResolver<T, R>,
    UserContextParameterResolver<T, R> {
        
    private val defaultMessagesFactory: DefaultMessagesFactory = context.getService()

    final override val pattern: Pattern get() = userMentionPattern
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
        val id = args.filterNotNull()
            .singleOrNull().ifNullThrowInternal { "Pattern matched but no args were present" }
            .toLongOrNull().ifNullThrowInternal { "ID matched but was not a Long" }
        return retrieveOrNull(id, event.message)
    }

    final override fun resolve(
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): R? = transformEntities(optionMapping.asUser, optionMapping.asMember)

    final override suspend fun resolveSuspend(event: GenericComponentInteractionCreateEvent, arg: String): R? {
        val id = arg.toLongOrNull() ?: throwArgument("Invalid user id: $arg")
        val entity = retrieveOrNull(id, event.message)
        if (entity == null)
            event.reply_(defaultMessagesFactory.get(event).resolverUserNotFoundMsg, ephemeral = true).queue()

        return entity
    }

    final override fun resolve(info: UserCommandInfo, event: UserContextInteractionEvent): R? =
        transformEntities(event.target, event.targetMember)

    private suspend fun retrieveOrNull(userId: Long, message: Message): R? {
        val guild = message.guildOrNull
        val member = when {
            guild != null -> {
                message.mentions.members.findEntity(userId)
                    ?: guild.retrieveMemberOrNull(userId)
            }
            else -> null
        }

        val user = member?.user
            ?: message.mentions.users.findEntity(userId)
            ?: message.jda.retrieveUserOrNull(userId)

        if (user == null) {
            return logger.traceNull { "Could not resolve user with ID $userId in '${guild?.name}' (${guild?.id})" }
        }

        return transformEntities(user, member)
    }

    private val Message.guildOrNull get() = if (isFromGuild) guild else null

    private fun <T : ISnowflake> Collection<T>.findEntity(id: Long): T? =
        find { user -> user.idLong == id }

    protected abstract fun transformEntities(user: User, member: Member?): R?

    internal companion object {
        internal val userMentionPattern = Pattern.compile("<@(\\d+)>|(\\d+)")
    }
}