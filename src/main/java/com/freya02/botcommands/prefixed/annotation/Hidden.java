package com.freya02.botcommands.prefixed.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Use this annotation if you want the command to be hidden from everyone but the owners, <b>the 'help' command will not display it</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Hidden {}