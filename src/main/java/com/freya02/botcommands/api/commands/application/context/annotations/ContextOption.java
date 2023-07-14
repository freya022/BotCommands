package com.freya02.botcommands.api.commands.application.context.annotations;

import com.freya02.botcommands.api.commands.annotations.Optional;
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder;
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets a parameter as a context command option.
 *
 * @see Optional @Optional
 * @see Nullable @Nullable (same as @Optional but better)
 *
 * @see UserCommandBuilder#option(String) DSL equivalent (user context commands)
 * @see MessageCommandBuilder#option(String) DSL equivalent (message context commands)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ContextOption {
}
