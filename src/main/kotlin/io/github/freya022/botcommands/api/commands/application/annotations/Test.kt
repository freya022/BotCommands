package io.github.freya022.botcommands.api.commands.application.annotations

import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.core.config.BApplicationConfig

/**
 * Defines an **annotated** application command as being test-only.
 *
 * This means this application command will only be pushed in guilds
 * defined by [BApplicationConfig.testGuildIds] and [guildIds].
 *
 * **Note:** This only applies to top-level commands, for slash commands,
 * this means the annotation needs to be used alongside [@TopLevelSlashCommandData][TopLevelSlashCommandData].
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Test(@get:JvmName("value") vararg val guildIds: Long)
