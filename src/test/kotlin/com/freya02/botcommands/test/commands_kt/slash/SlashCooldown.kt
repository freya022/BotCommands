package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.CooldownScope
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_
import java.util.concurrent.TimeUnit

@CommandMarker
class SlashCooldown : ApplicationCommand() {
    @com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand(name = "cooldown_annotated")
    @Cooldown(cooldown = 5, unit = TimeUnit.SECONDS, cooldownScope = CooldownScope.GUILD)
    fun onSlashCooldown(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("cooldown") {
            cooldown {
                cooldown = 5
                unit = TimeUnit.SECONDS
                scope = CooldownScope.GUILD
            }

            function = ::onSlashCooldown
        }
    }
}