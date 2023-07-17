package com.freya02.botcommands.api.commands.application.context.annotations

import com.freya02.botcommands.api.commands.annotations.Optional;
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder;
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder;
import org.jetbrains.annotations.Nullable;

/**
 * Sets a parameter as a context command option.
 *
 * @see Optional @Optional
 *
 * @see Nullable @Nullable
 * @see UserCommandBuilder.option DSL equivalent (user context commands)
 * @see MessageCommandBuilder.option DSL equivalent (message context commands)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ContextOption
