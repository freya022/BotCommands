package io.github.freya022.botcommands.api.commands.application.context.annotations

import io.github.freya022.botcommands.api.commands.annotations.Optional
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandBuilder
import org.jetbrains.annotations.Nullable

/**
 * Sets a parameter as a context command option.
 *
 * @see Optional @Optional
 *
 * @see Nullable @Nullable
 * @see UserCommandBuilder.option DSL equivalent (user context commands)
 * @see MessageCommandBuilder.option DSL equivalent (message context commands)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ContextOption
