package com.freya02.botcommands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Use this annotation if you want the subcommand to not inherit the permissions of its parent
 *
 * <br>
 * <h2>This does not work on application commands</h2>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DontInheritPermissions { }