package com.freya02.botcommands.api.localization.annotations;

import com.freya02.botcommands.api.localization.context.AppLocalizationContext;
import com.freya02.botcommands.api.localization.context.TextLocalizationContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to specify the bundle name and path prefix for injected {@link TextLocalizationContext} / {@link AppLocalizationContext} parameters
 *
 * @see TextLocalizationContext
 * @see AppLocalizationContext
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalizationBundle {
    /**
     * Specifies the name of the localization bundle used by the localization context.
     */
    String value();

    /**
     * Specifies the prefix used by the localization context.
     * <p>
     * <b>Example:</b> If the method is annotated a prefix of {@code commands.ban} and you ask for {@code responses.cannot_ban},
     * then it will search for {@code commands.ban.responses.cannot_ban}
     */
    String prefix() default "";
}
