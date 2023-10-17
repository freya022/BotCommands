package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand

@Command
class SlashDynamicTypedResolver : ApplicationCommand() {
    // The point is this shouldn't work for any other type of Map, see MapResolverFactory, StringDoubleMapResolver
    @JDASlashCommand(name = "dynamic_typed_resolver")
    suspend fun onSlashDynamicTypedResolver(event: GuildSlashEvent, map: Map<String, Double>) {
        event.reply_("$map", ephemeral = true).await()
    }
}