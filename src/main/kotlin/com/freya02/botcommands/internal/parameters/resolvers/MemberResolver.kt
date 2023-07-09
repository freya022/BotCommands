package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.core.utils.onErrorResponse
import com.freya02.botcommands.api.core.utils.onErrorResponseException
import com.freya02.botcommands.api.parameters.*
import com.freya02.botcommands.internal.commands.application.context.user.UserCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation
import com.freya02.botcommands.internal.commands.prefixed.TextUtils
import com.freya02.botcommands.internal.components.ComponentDescriptor
import com.freya02.botcommands.internal.utils.throwInternal
import com.freya02.botcommands.internal.utils.throwUser
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern
import kotlin.reflect.KParameter

@Resolver
class MemberResolver : ParameterResolver<MemberResolver, Member>(Member::class),
    RegexParameterResolver<MemberResolver, Member>,
    SlashParameterResolver<MemberResolver, Member>,
    ComponentParameterResolver<MemberResolver, Member>,
    UserContextParameterResolver<MemberResolver, Member> {

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
    ): Member? {
        return runCatching {
            //Fastpath for mentioned entities passed in the message
            val id = args[0]?.toLong() ?: throwInternal("Required pattern group is missing")

            TextUtils.findEntitySuspend(id, event.message.mentions.members) { event.guild.retrieveMemberById(id).await() }
        }.onErrorResponseException { e ->
            LOGGER.debug(
                "Could not resolve member in {} ({}): {} (regex command, may not be an error)",
                event.guild.name,
                event.guild.idLong,
                e.meaning
            )
        }.getOrNull()
    }

    override fun resolve(
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Member? = optionMapping.asMember

    override suspend fun resolveSuspend(
        context: BContext,
        descriptor: ComponentDescriptor,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): Member? {
        val guild = event.guild ?: throwUser("Can't get a member from DMs")

        return runCatching {
            guild.retrieveMemberById(arg).await()
        }.onErrorResponse { e ->
            LOGGER.error("Could not resolve member in {} ({}): {}", guild.name, guild.id, e.meaning)
        }.getOrNull()
    }

    override fun resolve(context: BContext, info: UserCommandInfo, event: UserContextInteractionEvent): Member? {
        return event.targetMember
    }
}