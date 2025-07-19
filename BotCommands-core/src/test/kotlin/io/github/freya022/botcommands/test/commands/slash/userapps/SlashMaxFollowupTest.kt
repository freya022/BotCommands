package io.github.freya022.botcommands.test.commands.slash.userapps

import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.reply_
import dev.freya02.botcommands.jda.ktx.requests.awaitCatching
import dev.freya02.botcommands.jda.ktx.requests.onErrorResponse
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.requests.ErrorResponse

@Command
class SlashMaxFollowupTest : ApplicationCommand() {
    @TopLevelSlashCommandData(contexts = [InteractionContextType.GUILD], integrationTypes = [IntegrationType.USER_INSTALL])
    @JDASlashCommand(name = "max_follow_up", description = "Follow up go brr")
    suspend fun onSlashMaxFollowUp(event: GuildSlashEvent) {
        event.reply_("Testing...", ephemeral = false).await()

        for (i in 1..5) {
            event.hook.sendMessage("$i").setEphemeral(true).queue()
        }
        event.hook.sendMessage("6").setEphemeral(true).awaitCatching()
            .onErrorResponse(ErrorResponse.MAX_FOLLOW_UP_MESSAGES_HIT) {
                println("MAX_FOLLOW_UP_MESSAGES_HIT success")
            }
            .onSuccess {
                throw AssertionError("Should not be allowed")
            }
    }
}
