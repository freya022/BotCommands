package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.commands.text.annotations.RequiresTextCommands
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveBestReference
import io.github.freya022.botcommands.internal.utils.rethrowAt
import io.github.oshai.kotlinlogging.KotlinLogging

@BService
@RequiresTextCommands
internal class TextCommandsBuilder internal constructor(
    context: BContextImpl,
    providers: List<TextCommandProvider>,
) {
    init {
        val logger = KotlinLogging.logger { }
        val manager = TextCommandManager(context)
        providers.forEach { textCommandProvider ->
            try {
                textCommandProvider.declareTextCommands(manager)
            } catch (e: Throwable) {
                e.rethrowAt("An error occurred while running a text command provider", textCommandProvider::declareTextCommands.resolveBestReference())
            }
        }

        val textCommands = manager.textCommands.values
        if (logger.isTraceEnabled()) {
            logger.trace {
                buildString {
                    appendLine("Loaded ${textCommands.size} text commands:")

                    fun TextCommandInfo.process(indent: Int) {
                        fun appendIndentedLine(string: String) {
                            append("\t".repeat(indent))
                            this@buildString.appendLine(string)
                        }

                        fun TextCommandVariation.isFallback() = completePattern == null

                        variations.forEach { variation ->
                            if (variation.completePattern != null) {
                                appendIndentedLine("- Pattern: ${variation.completePattern}")
                            } else {
                                appendIndentedLine("- Fallback")
                            }
                            appendIndentedLine("  Declared at: ${variation.declarationSite}")
                        }

                        if (variations.none { it.isFallback() }) {
                            appendIndentedLine("[No fallback]")
                        }

                        subcommands.values.forEach {
                            appendIndentedLine("${it.name}:")
                            it.process(indent + 1)
                        }
                    }

                    textCommands.forEach {
                        appendLine("${it.name}:")
                        it.process(1)
                    }
                }
            }
        } else {
            logger.debug { "Loaded ${textCommands.size} text commands" }
        }

        textCommands.forEach { context.textCommandsContext.addTextCommand(it) }
    }
}