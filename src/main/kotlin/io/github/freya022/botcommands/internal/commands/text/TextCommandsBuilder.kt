package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.declaration.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.declaration.TextCommandsDeclaration
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.events.FirstGuildReadyEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getInterfacedServices
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.oshai.kotlinlogging.KotlinLogging

@BService
internal class TextCommandsBuilder {
    @BEventListener
    internal fun onFirstReady(event: FirstGuildReadyEvent, context: BContextImpl) {
        try {
            val manager = TextCommandManager(context)
            context.serviceContainer
                .getInterfacedServices<TextCommandsDeclaration>()
                .forEach { textCommandsDeclaration ->
                    textCommandsDeclaration.declareTextCommands(manager)
                }

            manager.textCommands.map.values.forEach { context.textCommandsContext.addTextCommand(it) }
        } catch (e: Throwable) {
            KotlinLogging.logger { }.error(e) { "An error occurred while updating text commands" }
        } finally {
            context.eventDispatcher.removeEventListener(this)
        }
    }
}