package io.github.freya022.botcommands.api.core.db

import io.github.freya022.botcommands.internal.core.db.traced.TracedPreparedStatement
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.toKLogger
import org.slf4j.Logger
import java.sql.PreparedStatement

/**
 * Sets the logger used to log the query.
 *
 * This is a no-op if tracing is not enabled, see the full conditions on [Database].
 */
fun PreparedStatement.withLogger(logger: KLogger) {
    if (this.isWrapperFor(TracedPreparedStatement::class.java))
        this.unwrap(TracedPreparedStatement::class.java).logger = logger
}
/**
 * Sets the logger used to log the query.
 *
 * This is a no-op if tracing is not enabled, see the full conditions on [Database].
 */
fun PreparedStatement.withLogger(logger: Logger) = withLogger(logger.toKLogger())
/**
 * Sets the logger used to log the query.
 *
 * This is a no-op if tracing is not enabled, see the full conditions on [Database].
 */
fun PreparedStatement.withLogger(name: String) = withLogger(KotlinLogging.logger(name))