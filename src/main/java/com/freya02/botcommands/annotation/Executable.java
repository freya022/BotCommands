package com.freya02.botcommands.annotation;

import com.freya02.botcommands.Emoji;
import net.dv8tion.jda.api.entities.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to denote a method that needs to be translated to a regex<br>
 * <b>The arguments of the method must start with a BaseCommandEvent</b> and followed by any number of these classes (no arrays are allowed)
 * <ul>
 *     <li>{@linkplain String}</li>
 *
 *     <li>int</li>
 *     <li>long</li>
 *     <li>float</li>
 *     <li>double</li>
 *
 *     <li>{@linkplain Emoji}</li>
 *     <li>{@linkplain Emote}</li>
 *
 *     <li>{@linkplain Guild}</li>
 *
 *     <li>{@linkplain Role}</li>
 *     <li>{@linkplain User}</li>
 *     <li>{@linkplain Member}</li>
 *     <li>{@linkplain TextChannel}</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Executable {
	/**
	 * Specifies the specific order the executable has to be loaded in
	 * @return The order to be loaded
	 */
	int order() default 0;
}