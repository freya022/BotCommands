package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfo

/**
 * Must be implemented by your custom help command.
 *
 * If an implementation of that interface is found, it replaces the built-in help command.
 *
 * This lets the framework use the command to also display help about specific commands.
 *
 * **Usage**: Register your instance as a service with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
interface IHelpCommand {
    /**
     * Fired when a command is recognized, but the arguments do not correspond to any command variation.
     *
     * @param event       The event of the current command invocation
     * @param commandInfo The command info of the command which the user tried to use
     */
    fun onInvalidCommand(event: BaseCommandEvent, commandInfo: TextCommandInfo): Unit =
        throw NotImplementedError("${this.javaClass.simpleName} must implement the 'onInvalidCommand' or 'onInvalidCommandSuspend' method")

    /**
     * Fired when a command is recognized, but the arguments do not correspond to any command variation.
     *
     * @param event       The event of the current command invocation
     * @param commandInfo The command info of the command which the user tried to use
     */
    @JvmSynthetic
    suspend fun onInvalidCommandSuspend(event: BaseCommandEvent, commandInfo: TextCommandInfo): Unit =
        onInvalidCommand(event, commandInfo)
}
