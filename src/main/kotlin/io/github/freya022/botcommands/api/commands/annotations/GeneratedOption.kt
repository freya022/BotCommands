package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier

/**
 * Marks a parameter as being a generated option.
 *
 * ## For text commands
 * You will have to override [TextCommand.getGeneratedValueSupplier]
 * and return, on the correct command path/option name,
 * an appropriate [TextGeneratedValueSupplier] that will generate an object of the correct type.
 *
 * ## For application commands
 * You will have to override [ApplicationCommand.getGeneratedValueSupplier]
 * and return, on the correct guild/command id/command path/option name,
 * an appropriate [ApplicationGeneratedValueSupplier] that will generate an object of the correct type.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class GeneratedOption