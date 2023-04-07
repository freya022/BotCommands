package com.freya02.botcommands.api.annotations;

import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables you to suppress unused warnings for reflectively accessed classes, such as (application) commands.
 * <br>IDEs such as IntelliJ will suggest you a quick-fix to ignore unused warnings if annotated with <code>@CommandMarker</code>.
 * <br><i>This also works the same as with other annotations such as {@link JDATextCommand @JDATextCommand} on methods</i>.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CommandMarker { }