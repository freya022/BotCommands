package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*

internal object LocalizationUtils {
    private val logger = KotlinLogging.logger { }

    internal fun getCommandDescription(context: BContext, builder: INamedCommand, builderDescription: String?): String {
        val rootDescription = getCommandRootDescription(context, builder)
        return if (builderDescription != null) {
            // If a description was set, then use it, but check if a root description was found too
            if (rootDescription != null) {
                logger.debug { "A command description was set manually, while a root description was found in a localization bundle, path: '${builder.path}'" }
            }
            builderDescription
        } else {
            // If a description isn't set, then take the root description if it exists,
            // otherwise use the default description
            rootDescription ?: "No description"
        }
    }

    private fun getCommandRootDescription(context: BContext, command: INamedCommand): String? {
        val joinedPath = command.path.getFullPath('.')
        return getRootLocalization(context, "$joinedPath.description")
    }

    internal fun getOptionDescription(context: BContext, optionBuilder: SlashCommandOptionBuilder): String {
        val rootDescription = getOptionRootDescription(context, optionBuilder)
        val optionDescription = optionBuilder.description
        return if (optionDescription != null) {
            // If a description was set, then use it, but check if a root description was found too
            if (rootDescription != null) {
                logger.debug { "An option description was set manually, while a root description was found in a localization bundle, path: '${optionBuilder.commandBuilder.path}', option: '${optionBuilder.optionName}'" }
            }
            optionDescription
        } else {
            // If a description isn't set, then take the root description if it exists,
            // otherwise use the default description
            rootDescription ?: "No description"
        }
    }

    private fun getOptionRootDescription(context: BContext, optionBuilder: SlashCommandOptionBuilder): String? {
        val joinedPath = optionBuilder.commandBuilder.path.getFullPath('.')
        return getRootLocalization(context, "$joinedPath.options.${optionBuilder.optionName}.description")
    }

    private fun getRootLocalization(context: BContext, path: String): String? {
        val localizationService = context.getService<LocalizationService>()
        for (baseName in context.applicationConfig.baseNameToLocalesMap.keys) {
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