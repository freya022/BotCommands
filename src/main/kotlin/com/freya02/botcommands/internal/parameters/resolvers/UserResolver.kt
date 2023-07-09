package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.core.utils.onErrorResponseException
import com.freya02.botcommands.api.parameters.*
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation
import com.freya02.botcommands.internal.commands.prefixed.TextUtils
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.utils.throwInternal
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern
import kotlin.reflect.KParameter

@Resolver
class UserResolver : ParameterResolver<UserResolver, User>(User::class),
    RegexParameterResolver<UserResolver, User>,
    SlashParameterResolver<UserResolver, User>,
    ComponentParameterResolver<UserResolver, User>,
    UserContextParameterResolver<UserResolver, User> {

    override val pattern: Pattern = Pattern.compile("(?:<@!?)?(\\d+)>?")

    override val testExample: String = "<@1234>"
    override val optionType: OptionType = OptionType.USER

    override fun getHelpExample(parameter: KParameter, event: BaseCommandEvent, isID: Boolean): String {
        return event.member.asMention
    }

    override suspend fun resolveSuspend(
        context: BContext,
        variation: TextCommandVariation,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): User? {
        return runCatching {
            //Fastpath for mentioned entities passed in the message
            val id = args[0]?.toLong() ?: throwInternal("Required pattern group is missing")

            TextUtils.findEntitySuspend(id, event.message.mentions.users) { event.jda.retrieveUserById(id).await() }
        }.onErrorResponseException { e ->
            LOGGER.error("Could not resolve user: {}", e.meaning)
        }.getOrNull()
    }

    override fun resolve(context: BContext, info: SlashCommandInfo, event: CommandInteractionPayload, optionMapping: OptionMapping): User {
        return optionMapping.asUser
    }

    override suspend fun resolveSuspend(
        context: BContext,
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): User? {
        return runCatching {
            event.jda.retrieveUserById(arg).await()
        }.onErrorResponseException { e ->
            LOGGER.error("Could not resolve user: {}", e.meaning)
        }.getOrNull()
    }

    override fun resolve(context: BContext, info: UserCommandInfo, event: UserContextInteractionEvent): User {
        return event.target
    }
}