package com.freya02.botcommands.internal.components.sql

import com.freya02.botcommands.api.components.FetchResult
import com.freya02.botcommands.internal.core.db.Transaction
import com.freya02.botcommands.internal.throwInternal

internal class SQLFetchResult(val fetchedComponent: SQLFetchedComponent?, transaction: Transaction?) : FetchResult(fetchedComponent) {
    private var closed = false

    private val _transaction = transaction
    val transaction: Transaction
        get() {
            check(!closed) { "Cannot get Transaction from SQLFetchedComponent as it has been closed" }
            return _transaction ?: throwInternal("Transaction was used when the fetch result was erroneous")
        }

    @Throws(Exception::class)
    override fun close() {
        closed = true
    }
}