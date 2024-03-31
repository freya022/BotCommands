package io.github.freya022.botcommands.api.core.annotations

import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.core.db.Database

/**
 * Ignores stack frames of this class.
 *
 * ## Use in [Database]
 * This is useful when retrieving the logger of traced prepared statements,
 * see [Database] for details on how tracing works.
 *
 * ## When declaring commands
 * When an error happens while declaring a command (such as when one is duplicated),
 * the declaration site is used to tell where a command was created at.
 *
 * See [DeclarationSite].
 *
 * @see Database
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreStackFrame