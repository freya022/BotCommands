package com.freya02.botcommands.slash.annotations;

import java.lang.annotation.*;

/**
 * One of the choices in a list of {@linkplain Choices}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Repeatable(value = Choices.class)
public @interface Choice {
	String name();
	String value() default "";
	int intValue() default 0;
}
