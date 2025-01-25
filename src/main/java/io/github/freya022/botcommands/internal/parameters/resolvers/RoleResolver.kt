package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.components.options.ComponentOption
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.regex.Pattern

/**
 * @see RoleResolverFactoryProvider
 */
class RoleResolver : ClassParameterResolver<RoleResolver, Role>(Role::class),
                     TextParameterResolver<RoleResolver, Role>,
                     SlashParameterResolver<RoleResolver, Role>,
                     ComponentParameterResolver<RoleResolver, Role> {

    override val pattern: Pattern = Pattern.compile("<@&(\\d+)>|(\\d+)")
    override val testExample: String = "<@&1234>"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String {
        return event.member.roles.stream().findAny()
            .or { event.guild.roleCache.streamUnordered().findAny() }
            .map { obj: Role -> obj.asMention }
            .orElse("role-id/mention")
    }

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>
    ): Role? {
        val id = args[0] ?: args[1] ?: throwInternal("How can it not have either")
        if (event.guild.id == id) return null //@everyone role

        return event.guild.getRoleById(id)
    }


    override val optionType: OptionType get() = OptionType.ROLE

    override suspend fun resolveSuspend(
        option: SlashCommandOption,
        event: CommandInteractionPayload,
        optionMapping: OptionMapping
    ): Role = optionMapping.asRole

    override suspend fun resolveSuspend(
        option: ComponentOption,
        event: GenericComponentInteractionCreateEvent,
        arg: String
    ): Role? {
        val guild = event.guild
        requireNotNull(guild) { "Can't get a role from DMs" }

        return guild.getRoleById(arg)
    }
}
