package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.CooldownScope
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import dev.minn.jda.ktx.messages.reply_
import java.util.concurrent.TimeUnit

@Command
class SlashCooldown : ApplicationCommand() {
    @JDASlashCommand(name = "cooldown_annotated")
    @Cooldown(cooldown = 5, unit = TimeUnit.SECONDS, cooldownScope = CooldownScope.GUILD)
    fun onSlashCooldown(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("cooldown", function = ::onSlashCooldown) {
            cooldown {
                cooldown = 5
                unit = TimeUnit.SECONDS
                scope = CooldownScope.GUILD
            }
        }
    }
}