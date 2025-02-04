package io.github.freya022.botcommands.api.commands.application.diff

import io.github.freya022.botcommands.api.core.config.application.cache.ApplicationCommandsCacheConfig
import io.github.freya022.botcommands.internal.commands.application.diff.ApplicationCommandDiffEngine
import io.github.freya022.botcommands.internal.commands.application.diff.NewApplicationCommandDiffEngine

/**
 * Represents predefined implementation of an application command diff engine.
 *
 * @see ApplicationCommandsCacheConfig.diffEngine
 */
enum class DiffEngine(@get:JvmSynthetic internal val instance: ApplicationCommandDiffEngine) {
    /**
     * Cleaner logs with what exactly changed.
     */
    NEW(NewApplicationCommandDiffEngine)
}