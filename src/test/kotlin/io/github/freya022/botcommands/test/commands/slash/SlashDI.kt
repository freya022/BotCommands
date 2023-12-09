package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.text.IHelpCommand
import io.github.freya022.botcommands.api.core.DefaultEmbedSupplier
import io.github.freya022.botcommands.api.core.db.BlockingDatabase
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName
import io.github.freya022.botcommands.internal.core.ReadyListener
import io.github.freya022.botcommands.test.services.UnusedInterfacedService

@Command
class SlashDI internal constructor(
    @ServiceName("builtinHelpCommand") builtinHelpCommand: IHelpCommand?,
    @ServiceName("fakeDefaultEmbedSupplier") defaultService: DefaultEmbedSupplier = DefaultEmbedSupplier.Default(),
    unusedInterfacedService: UnusedInterfacedService?
) : ApplicationCommand() {
    init {
        println("Built-in help command: $builtinHelpCommand")
        println("Default embed supplier: $defaultService")
        println("UnusedInterfacedService: $unusedInterfacedService")
    }

    @JDASlashCommand(name = "di")
    internal fun onSlashDi(
        event: GuildSlashEvent,
        filters: List<ApplicationCommandFilter<*>>,
        databaseLazy: Lazy<BlockingDatabase>,
        unusableLazy: Lazy<UnusedInterfacedService?>,
        @ServiceName("firstReadyListenerNope") inexistantListener: ReadyListener?,
        @ServiceName("fakeDefaultEmbedSupplier") defaultService: DefaultEmbedSupplier = DefaultEmbedSupplier.Default()
    ) {
        event.reply_(
            """
                Filters: $filters
                DB: ${databaseLazy.value}
                unusable: ${unusableLazy.value}
                inexistant listener: $inexistantListener
                embed supplier: $defaultService
            """.trimIndent(),
            ephemeral = true
        ).queue()
    }
}