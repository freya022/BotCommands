package io.github.freya022.botcommands.api.commands.application.context.annotations

import io.github.freya022.botcommands.api.commands.annotations.Optional
import io.github.freya022.botcommands.api.commands.application.context.message.options.builder.MessageCommandOptionRegistry
import io.github.freya022.botcommands.api.commands.application.context.user.options.builder.UserCommandOptionRegistry
import org.jetbrains.annotations.Nullable

/**
 * Sets a parameter as a context command option.
 *
 * @see Optional @Optional
 *
 * @see Nullable @Nullable
 * @see UserCommandOptionRegistry.option DSL equivalent (user context commands)
 * @see MessageCommandOptionRegistry.option DSL equivalent (message context commands)
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ContextOption
