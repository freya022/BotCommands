package com.freya02.botcommands.buttons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JdaButtonListener {
	/**
	 * Name of the button listener, this is used to find back the handler method after a button has been clicked
	 *
	 * @return Name of the button listener
	 */
	String name();
}