package io.github.freya022.botcommands.api.commands.text.provider

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Interface to declare text commands, ran once at startup.
 *
 * **Usage**: Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see Command @Command
 * @see JDATextCommandVariation @JDATextCommandVariation
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface TextCommandProvider {
    fun declareTextCommands(manager: TextCommandManager)
}