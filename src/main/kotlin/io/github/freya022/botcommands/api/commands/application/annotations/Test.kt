package io.github.freya022.botcommands.api.commands.application.annotations

import io.github.freya022.botcommands.api.core.config.BApplicationConfig
import net.dv8tion.jda.api.entities.Guild

/**
 * Defines an **annotated** application command as being test-only.
 *
 * This means this application command will only be pushed in guilds
 * defined by [BApplicationConfig.testGuildIds] and [guildIds].
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Test(
    /**
     * Specifies the [Guild] IDs in which the command should try to be inserted in
     */
    val guildIds: LongArray = [],

    /**
     * Whether this should be added to the list of existing test guild IDs
     *
     * **Default:** false
     */
    val append: Boolean = false
)
