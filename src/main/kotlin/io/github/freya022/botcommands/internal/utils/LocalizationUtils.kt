package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.LocalizationService
import java.util.*

internal object LocalizationUtils {
    internal fun getCommandRootDescription(context: BContext, commandBuilder: SlashCommandBuilder): String? {
        val joinedPath = commandBuilder.path.getFullPath('.')
        return getRootLocalization(context, "$joinedPath.description")
    }

    internal fun getOptionRootDescription(context: BContext, optionBuilder: SlashCommandOptionBuilder): String? {
        val joinedPath = optionBuilder.commandBuilder.path.getFullPath('.')
        return getRootLocalization(context, "$joinedPath.options.${optionBuilder.optionName}.description")
    }

    private fun getRootLocalization(context: BContext, path: String): String? {
        val localesMap: Map<String, List<Locale>> = context.applicationConfig.baseNameToLocalesMap
        val localizationService = context.getService<LocalizationService>()
        for (baseName in localesMap.keys) {
            val localization = localizationService.getInstance(baseName, Locale.ROOT)
            if (localization != null) {
                val template = localization[path]
                if (template != null) {
                    return template.localize()
                }
            }
        }

        return null
    }
}