package io.github.freya022.botcommands.api.commands.application.annotations

/**
 * Sets a **unique** command ID on an **annotated** application command function.
 *
 * This can be used to disambiguate some commands,
 * and can be applied to any application command, top-level or not.
 *
 * @see DeclarationFilter @DeclarationFilter
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandId(val value: String)
