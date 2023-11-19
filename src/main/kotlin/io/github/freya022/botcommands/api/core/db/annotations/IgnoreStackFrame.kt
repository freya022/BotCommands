package io.github.freya022.botcommands.api.core.db.annotations

import io.github.freya022.botcommands.api.core.db.Database

/**
 * Ignores stack frames from this class when retrieving the logger of traced prepared statements.
 *
 * See [Database] for details on how tracing works.
 *
 * @see Database
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreStackFrame