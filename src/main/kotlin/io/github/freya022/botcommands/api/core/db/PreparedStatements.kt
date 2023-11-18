package io.github.freya022.botcommands.api.core.db

import io.github.freya022.botcommands.internal.core.db.traced.TracedPreparedStatement
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.toKLogger
import org.slf4j.Logger
import java.sql.PreparedStatement

//TODO document
fun PreparedStatement.withLogger(logger: KLogger) {
    if (this.isWrapperFor(TracedPreparedStatement::class.java))
        this.unwrap(TracedPreparedStatement::class.java).logger = logger
}
fun PreparedStatement.withLogger(logger: Logger) = withLogger(logger.toKLogger())
fun PreparedStatement.withLogger(name: String) = withLogger(KotlinLogging.logger(name))