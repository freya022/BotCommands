package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.interactions.IntegrationType.*
import net.dv8tion.jda.api.interactions.InteractionContextType.GUILD

@Command
class SlashDetachedPermissions : ApplicationCommand(), GlobalApplicationCommandProvider {
    @JDASlashCommand("detached_permissions_annotated")
    @TopLevelSlashCommandData(
        contexts = [GUILD],
        integrationTypes = [GUILD_INSTALL, USER_INSTALL]
    )
    suspend fun onSlashDetachedPermissions(event: GuildSlashEvent, @SlashOption member: Member, @SlashOption role: Role, @SlashOption channel: TextChannel, @SlashOption channel2: TextChannel, @SlashOption thread: ThreadChannel?) {
        event.deferReply(true).await()

        event.hook.sendMessage("""
            $member
            $role
            $channel
            $channel2
            $thread
            """.trimIndent()).await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("detached_permissions", ::onSlashDetachedPermissions) {
            contexts = enumSetOf(GUILD)
            integrationTypes = ALL

            option("member")
            option("role")
            option("channel")
            option("channel2")
            option("thread")
        }
    }
}