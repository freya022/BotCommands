package com.freya02.botcommands.internal.components.sql

import com.freya02.botcommands.api.components.FetchResult
import java.sql.Connection

class SQLFetchResult(val fetchedComponent: SQLFetchedComponent?, private val connection: Connection) : FetchResult(fetchedComponent) {
    private var closed = false

    fun getConnection(): Connection {
        check(!closed) { "Cannot get Connection from SQLFetchedComponent as it has been closed" }

        return connection
    }

    @Throws(Exception::class)
    override fun close() {
        closed = true

        connection.close()
    }
}