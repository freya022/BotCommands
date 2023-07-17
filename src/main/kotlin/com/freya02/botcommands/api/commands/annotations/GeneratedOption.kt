package com.freya02.botcommands.api.commands.annotations

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
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class GeneratedOption  