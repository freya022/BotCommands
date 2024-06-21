package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.provider.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveBestReference
import io.github.freya022.botcommands.internal.utils.rethrowAt

@BService
internal class TextCommandsBuilder internal constructor(
    context: BContextImpl,
    providers: List<TextCommandProvider>,
) {
    init {
        val manager = TextCommandManager(context)
        providers.forEach { textCommandProvider ->
            try {
                textCommandProvider.declareTextCommands(manager)
            } catch (e: Throwable) {
                e.rethrowAt("An error occurred while running a text command provider", textCommandProvider::declareTextCommands.resolveBestReference())
            }
        }

        manager.textCommands.values.forEach { context.textCommandsContext.addTextCommand(it) }
    }
}