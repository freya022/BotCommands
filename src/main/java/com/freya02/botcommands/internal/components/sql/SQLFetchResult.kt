package com.freya02.botcommands.internal.components.sql

import com.freya02.botcommands.api.components.FetchResult
import com.freya02.botcommands.core.internal.db.Transaction
import com.freya02.botcommands.internal.throwInternal
import java.sql.Connection

internal class SQLFetchResult(val fetchedComponent: SQLFetchedComponent?, private val transaction: Transaction?) : FetchResult(fetchedComponent) {
    private var closed = false

    @Deprecated("Deprecated in favor of transaction")
    fun getConnection(): Connection {
        check(!closed) { "Cannot get Connection from SQLFetchedComponent as it has been closed" }

        return transaction?.connection ?: throwInternal("Connection was used when the fetch result was erroneous")
    }

    fun getTransaction(): Transaction {
        check(!closed) { "Cannot get Transaction from SQLFetchedComponent as it has been closed" }

        return transaction ?: throwInternal("Transaction was used when the fetch result was erroneous")
    }

    @Throws(Exception::class)
    override fun close() {
        closed = true

        transaction?.connection?.close()
    }
}