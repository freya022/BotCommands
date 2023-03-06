package com.freya02.botcommands.api.localization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a localization prefix at the class or method level.
 * <br>The method prefix has more priority than the class prefix.
 * <p>For example, if the method is annotated a prefix of <code>"ban.error_codes"</code> and you ask for <code>"cannot_ban"</code>, then it will search for <code>"ban.error_codes.cannot_ban"</code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LocalizationPrefix {
	String value();
}
