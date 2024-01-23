package io.github.freya022.botcommands.api.core.annotations

import io.github.freya022.botcommands.api.core.db.Database

/**
 * Ignores stack frames of this class.
 *
 * ## Use in [Database]
 * This is useful when retrieving the logger of traced prepared statements,
 * see [Database] for details on how tracing works.
 *
 * @see Database
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreStackFrame