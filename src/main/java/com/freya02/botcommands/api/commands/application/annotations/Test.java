package com.freya02.botcommands.api.commands.application.annotations

/**
 * Defines an **annotated** application command as being test-only.
 *
 * This means this application command will only be pushed in guilds
 * defined by [BApplicationConfig.testGuildIds] and [guildIds].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
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
