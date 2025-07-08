package dev.freya02.botcommands.typesafe.messages.integration

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.DefaultLocalizationMap
import io.github.freya022.botcommands.api.localization.DefaultLocalizationTemplate
import io.github.freya022.botcommands.api.localization.LocalizationMap
import io.github.freya022.botcommands.api.localization.LocalizationMapRequest
import io.github.freya022.botcommands.api.localization.readers.LocalizationMapReader

@BService
class MyBundleReader(
    private val context: BContext,
) : LocalizationMapReader {

    override fun readLocalizationMap(request: LocalizationMapRequest): LocalizationMap? {
        if (request.baseName != "myBundle") return null

        return DefaultLocalizationMap(
            request.requestedLocale, mapOf(
                "whats.the.fox.doing" to DefaultLocalizationTemplate(
                    context,
                    "The fox quickly {action}",
                    request.requestedLocale
                ),
            )
        )
    }
}
