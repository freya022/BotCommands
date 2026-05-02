package io.github.freya022.botcommands.internal.utils

internal object LocalizationUtils {

    internal fun getEffectivePath(localizationPrefix: String?, localizationPath: String): String {
        if (localizationPath.startsWith('/')) return localizationPath.substring(1)

        return when (localizationPrefix) {
            null -> localizationPath
            else -> "$localizationPrefix.$localizationPath"
        }
    }
}
