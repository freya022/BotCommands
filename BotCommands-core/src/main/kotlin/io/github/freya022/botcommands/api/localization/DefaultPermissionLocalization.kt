package io.github.freya022.botcommands.api.localization

import net.dv8tion.jda.api.Permission
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Default implementation for [PermissionLocalization].
 *
 * The translations are queried from [bundleName],
 * and the keys used are the enum name of the permission.
 *
 * The returned translations will try to use the nearest available locale,
 * with the last fallback being [Permission.getName].
 *
 * ### Light customization / Supporting more locales
 *
 * To support more locales, (here we supposed the [bundleName] is `Permissions`) you may create a new `Permissions.json`
 * following by appending an underscore and the [language tag][Locale.toLanguageTag],
 * such as `Permissions_fr.json`.
 *
 * The localization paths must be equal to [Permission.getName].
 *
 * Refer to [Localization] for mode customization details.
 */
open class DefaultPermissionLocalization(
    protected val localizationService: LocalizationService,
    protected val bundleName: String = "Permissions",
) : PermissionLocalization {

    private val cache: MutableMap<CacheKey, String> = ConcurrentHashMap()

    override fun localize(permission: Permission, locale: Locale): String {
        val key = CacheKey(permission, locale)
        return cache.getOrPut(key) {
            val permissionsLocalization: Localization? = localizationService.getInstance(bundleName, locale)

            @Suppress("UsePropertyAccessSyntax") // `permission.name` targets Enum#name() which is definitely not the same
            permissionsLocalization?.get(permission.name)?.localize() ?: permission.getName()
        }
    }

    private data class CacheKey(val permission: Permission, val locale: Locale)
}