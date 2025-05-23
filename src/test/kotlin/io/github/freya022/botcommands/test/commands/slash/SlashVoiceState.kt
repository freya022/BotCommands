package io.github.freya022.botcommands.test.commands.slash

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.utils.reply

@Command
class SlashVoiceState : ApplicationCommand() {

    @JDASlashCommand(name = "voice_state")
    fun onSlashVoiceState(event: GuildSlashEvent) {
        event.reply(ephemeral = true) {
            content = """
                Cached member: ${event.guild.getMember(event.member)}
                Voice state: ${event.member.voiceState}
                Voice state member: ${event.member.voiceState?.member}
                Voice state channel: ${event.member.voiceState?.channel}
                Voice state deafened: ${event.member.voiceState?.isDeafened}
                Voice state muted: ${event.member.voiceState?.isMuted}
                Voice state self deafened: ${event.member.voiceState?.isSelfDeafened}
                Voice state self muted: ${event.member.voiceState?.isSelfMuted}
                Voice state guild deafened: ${event.member.voiceState?.isGuildDeafened}
                Voice state guild muted: ${event.member.voiceState?.isGuildMuted}
                Voice state suppressed: ${event.member.voiceState?.isSuppressed}
                Voice state stream: ${event.member.voiceState?.isStream}
                Voice state sending video: ${event.member.voiceState?.isSendingVideo}
            """.trimIndent()
        }.queue()
    }
}