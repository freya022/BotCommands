package com.freya02.botcommands.api.commands.annotations

/**
 * Marks an option ([@SlashOption][SlashOption] or [@TextOption][TextOption]) as being optional.
 *
 * **Note**: Kotlin users are not required to use this annotation, using `?` is enough.
 *
 * I recommend using [@Nullable][Nullable] annotation instead, to benefit from nullability analysis.
 *
 * **Note for text commands:** Take care with this annotation, you might have errors if your command is considered too complex.<br>
 * Several factors can increase the chance of a command being unusable, such as
 * - Too many optionals
 * - Options with a dynamic number of spaces (such as strings)
 *
 * Attempts at fixing the issue can include moving the parameters around, like avoiding 2 strings next to each other.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Optional  