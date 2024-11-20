package io.github.freya022.botcommands.test.commands.slash.userapps

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

@Command
class SlashPrivateChannelInBot : ApplicationCommand() {

    @TopLevelSlashCommandData(contexts = [InteractionContextType.PRIVATE_CHANNEL], integrationTypes = [IntegrationType.USER_INSTALL])
    @JDASlashCommand(name = "private_channel_in_bot")
    suspend fun onSlashPrivateChannelInBot(event: GlobalSlashEvent) {
        event.reply_("OK", ephemeral = true).queue()
    }
}