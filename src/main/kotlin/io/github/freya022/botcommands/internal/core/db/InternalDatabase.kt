package io.github.freya022.botcommands.internal.core.db

import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.IgnoreServiceTypes

@BService
@RequiresDatabase
@IgnoreServiceTypes(Database::class)
internal class InternalDatabase internal constructor(private val database: Database) : Database by database {
    override suspend fun fetchConnection(readOnly: Boolean) = database.fetchConnection().also {
        it.schema = "bc"
    }
}