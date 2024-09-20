package io.github.freya022.botcommands.api.commands.application.annotations

import io.github.freya022.botcommands.api.commands.application.CommandDeclarationFilter
import io.github.freya022.botcommands.api.commands.application.CommandScope
import kotlin.reflect.KClass

/**
 * Runs the following filters before declaring the annotated application command.
 *
 * Only works on [guild][CommandScope.GUILD] commands.
 *
 * ### Merging
 * This annotation can be merged if found with other meta-annotations.
 * Keep in mind that a *direct* annotation overrides all meta-annotations.
 *
 * @see CommandDeclarationFilter
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DeclarationFilter(@get:JvmName("value") vararg val filters: KClass<out CommandDeclarationFilter>)
