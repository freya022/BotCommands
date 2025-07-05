package io.github.freya022.botcommands.test.commands.slash.userapps

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import net.dv8tion.jda.api.entities.channel.attribute.ICopyableChannel
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

@Command
class SlashStealChannel : ApplicationCommand() {

    @TopLevelSlashCommandData(integrationTypes = [IntegrationType.USER_INSTALL], contexts = [InteractionContextType.GUILD])
    @JDASlashCommand(name = "steal_channel")
    suspend fun onSlashStealChannel(event: GuildSlashEvent, @SlashOption channel: ICopyableChannel) {
        channel.createCopy(event.jda.getGuildById(722891685755093072)!!)
            .setParent(event.jda.getCategoryById(1274378097671798945)!!)
            .await()

        event.reply("Scammed")
            .setEphemeral(true)
            .queue()
    }
}