package com.freya02.botcommands.api.core.db

import org.intellij.lang.annotations.Language
import java.sql.Connection

class KConnection internal constructor(val database: Database, val connection: Connection) : Connection by connection {
    inline fun <R> preparedStatement(@Language("PostgreSQL") sql: String, block: KPreparedStatement.() -> R): R {
        return block(KPreparedStatement(database, prepareStatement(sql)))
    }

    override fun toString(): String {
        return connection.toString()
    }
}