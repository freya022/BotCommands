package com.freya02.botcommands.prefixed.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Use this annotation if you want the 'help' command to add a field in the help embed containing subcommand names and descriptions
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AddSubcommandHelp {}