package com.freya02.botcommands.internal.core.db

import org.intellij.lang.annotations.Language
import java.sql.Connection

class Transaction(val database: Database, val connection: Connection) {
    inline fun <R> preparedStatement(@Language("PostgreSQL") sql: String, block: KPreparedStatement.() -> R): R {
        return block(KPreparedStatement(database, connection.prepareStatement(sql)))
    }
}