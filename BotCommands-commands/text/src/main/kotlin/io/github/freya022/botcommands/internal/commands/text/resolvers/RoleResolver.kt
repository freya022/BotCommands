package io.github.freya022.botcommands.internal.commands.text.resolvers

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.commands.text.options.TextCommandOption
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

@Resolver
@ServiceName("textCommandRoleResolver")
@RequiresTextCommands
internal class RoleResolver : ClassParameterResolver<RoleResolver, Role>(Role::class),
                              TextParameterResolver<RoleResolver, Role> {

    override val pattern: Pattern = Pattern.compile("<@&(\\d+)>|(\\d+)")
    override val testExample: String = "<@&1234>"
    override fun getHelpExample(option: TextCommandOption, event: BaseCommandEvent): String {
        val role = event.member.unsortedRoles.firstOrNull() ?: event.guild.roleCache.first()
        return role?.asMention ?: "role-id/mention"
    }

    override suspend fun resolveSuspend(
        option: TextCommandOption,
        event: MessageReceivedEvent,
        args: Array<String?>,
    ): Role? {
        val id = args[0] ?: args[1] ?: throwInternal("How can it not have either")
        if (event.guild.id == id) return null //@everyone role

        return event.guild.getRoleById(id)
    }
}
