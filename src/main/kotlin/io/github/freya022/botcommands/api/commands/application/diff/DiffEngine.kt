package io.github.freya022.botcommands.api.commands.application.diff

import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import io.github.freya022.botcommands.internal.commands.application.diff.ApplicationCommandDiffEngine
import io.github.freya022.botcommands.internal.commands.application.diff.NewApplicationCommandDiffEngine
import io.github.freya022.botcommands.internal.commands.application.diff.OldApplicationCommandDiffEngine
import io.github.freya022.botcommands.internal.commands.application.diff.OldRefactoredApplicationCommandDiffEngine

/**
 * Represents predefined implementation of an application command diff engine.
 *
 * @see BApplicationConfig.diffEngine
 */
enum class DiffEngine(@get:JvmSynthetic internal val instance: ApplicationCommandDiffEngine) {
    /**
     * Good ol' engine.
     */
    @Deprecated(message = "Only use this if there is a bug with the new engine, may be removed in a future release.")
    OLD(OldApplicationCommandDiffEngine),

    /**
     * Provides even more logs.
     */
    @Deprecated(message = "Only use this if there is a bug with the new engine, may be removed in a future release.")
    OLD_REFACTORED(OldRefactoredApplicationCommandDiffEngine),

    /**
     * Cleaner logs with what exactly changed.
     */
    NEW(NewApplicationCommandDiffEngine)
}