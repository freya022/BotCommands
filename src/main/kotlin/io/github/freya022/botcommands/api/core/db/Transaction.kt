package io.github.freya022.botcommands.api.core.db

import org.intellij.lang.annotations.Language
import java.sql.Connection

class Transaction @PublishedApi internal constructor(val connection: Connection) {
    @Suppress("SqlSourceToSinkFlow")
    inline fun <R> preparedStatement(@Language("PostgreSQL") sql: String, block: KPreparedStatement.() -> R): R {
        return KPreparedStatement(connection.prepareStatement(sql)).use(block)
    }
}