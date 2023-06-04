package com.freya02.botcommands.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation if you want to instantiate this command only on certain conditions.
 * <br>Put this on a method returning a boolean, with no parameters, if you return <code>true</code>, the command will be created
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ConditionalUse {} //TODO remove