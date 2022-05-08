package com.freya02.botcommands.annotations.api.annotations;

import com.freya02.botcommands.api.CooldownScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Specifies the cooldown of this text / application command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Cooldown {
	/**
	 * Cooldown time {@link #unit in the specified unit} before the command can be used again in the scope specified by {@link #cooldownScope()}
	 *
	 * @return Cooldown time {@link #unit in the specified unit}
	 */
	int cooldown() default 0;

	/**
	 * The time unit of the cooldown
	 *
	 * @return The {@link TimeUnit} of the cooldown
	 */
	TimeUnit unit() default TimeUnit.MILLISECONDS;

	/**
	 * Scope of the cooldown, can be either {@linkplain CooldownScope#USER}, {@linkplain CooldownScope#CHANNEL} or {@linkplain CooldownScope#GUILD}
	 *
	 * @return Scope of the cooldown
	 */
	CooldownScope cooldownScope() default CooldownScope.USER;
}
