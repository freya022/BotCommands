package com.freya02.botcommands.api.localization.annotations

import com.freya02.botcommands.api.localization.context.AppLocalizationContext;
import com.freya02.botcommands.api.localization.context.TextLocalizationContext;

/**
 * Sets the bundle name and path prefix for injected [TextLocalizationContext] / [AppLocalizationContext] parameters
 *
 * @see TextLocalizationContext
 * @see AppLocalizationContext
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LocalizationBundle(
    /**
     * Specifies the name of the localization bundle used by the localization context.
     */
    val value: String,

    /**
     * Specifies the prefix used by the localization context.
     *
     * **Example:** If the method is annotated a prefix of `commands.ban` and you ask for `responses.cannot_ban`,
     * then it will search for `commands.ban.responses.cannot_ban`
     */
    val prefix: String = ""
)
