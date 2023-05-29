package com.freya02.botcommands.internal.core.db

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.core.db.Database

@BService
@ConditionalService(dependencies = [Database::class])
internal class InternalDatabase internal constructor(private val database: Database) : Database by database {
    override suspend fun fetchConnection(readOnly: Boolean) = database.fetchConnection().also {
        it.schema = "bc"
    }
}