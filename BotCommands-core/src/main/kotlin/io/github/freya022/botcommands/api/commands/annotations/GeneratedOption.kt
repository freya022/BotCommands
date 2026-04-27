package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplierProvider

/**
 * Marks a parameter as being a generated option.
 *
 * ## For application commands
 * You will have to implement [ApplicationGeneratedValueSupplierProvider]
 * and return, on the correct guild/command id/command path/option name,
 * an appropriate [ApplicationGeneratedValueSupplier] that will generate an object of the correct type.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class GeneratedOption
