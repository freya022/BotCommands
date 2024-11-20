package io.github.freya022.botcommands.api.localization.readers

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.localization.DefaultLocalizationTemplate
import io.github.freya022.botcommands.api.localization.LocalizationTemplate
import io.github.freya022.botcommands.api.localization.readers.LocalizationTemplateFunction.Companion.createDefault
import java.util.*

/**
 * Converts a localization template and its locale into a [LocalizationTemplate].
 *
 * @see createDefault
 */
fun interface LocalizationTemplateFunction {

    fun apply(template: String, locale: Locale): LocalizationTemplate

    companion object {

        /**
         * Creates a [LocalizationTemplateFunction] returning [DefaultLocalizationTemplate].
         */
        @JvmStatic
        fun createDefault(context: BContext): LocalizationTemplateFunction =
            LocalizationTemplateFunction { template, locale ->
                DefaultLocalizationTemplate(context, template, locale)
            }
    }
}