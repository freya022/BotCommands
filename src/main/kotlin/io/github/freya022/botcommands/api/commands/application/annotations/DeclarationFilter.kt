package io.github.freya022.botcommands.api.commands.application.annotations

import io.github.freya022.botcommands.api.commands.application.CommandDeclarationFilter
import io.github.freya022.botcommands.api.commands.application.CommandScope
import kotlin.reflect.KClass

/**
 * Runs the following filters before declaring the annotated application command.
 *
 * Only works on [guild][CommandScope.GUILD] commands.
 *
 * @see CommandDeclarationFilter
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DeclarationFilter(@get:JvmName("value") vararg val filters: KClass<out CommandDeclarationFilter>)
