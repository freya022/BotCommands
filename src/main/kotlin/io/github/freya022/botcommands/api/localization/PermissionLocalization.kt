package io.github.freya022.botcommands.api.localization

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.Permission
import java.util.*

/**
 * Utility service to translate [Permission] names, a [DefaultPermissionLocalization] instance is available by default,
 * but can be overridden if necessary, using a service factory.
 */
@InterfacedService(acceptMultiple = false)
interface PermissionLocalization {

    /**
     * Returns the given permission's name with the requested locale.
     *
     * If no translation is available for the requested locale,
     * it is permitted to return fallbacks.
     */
    fun localize(permission: Permission, locale: Locale): String
}