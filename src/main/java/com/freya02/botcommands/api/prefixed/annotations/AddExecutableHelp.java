package com.freya02.botcommands.api.prefixed.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Use this annotation if you want the 'help' command to add fields in the help embed containing executable methods signatures (per a discord user) and examples
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AddExecutableHelp { }

//TODO remove