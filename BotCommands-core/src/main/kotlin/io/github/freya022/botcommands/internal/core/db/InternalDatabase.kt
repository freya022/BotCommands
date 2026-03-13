package io.github.freya022.botcommands.internal.core.db

import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.IgnoreServiceTypes
import java.sql.Connection

@BService
@RequiresDatabase
@IgnoreServiceTypes(Database::class)
internal class InternalDatabase internal constructor(private val database: Database) : Database by database {
    override suspend fun fetchConnection(readOnly: Boolean): Connection {
        val connection = database.fetchConnection()
        return object : Connection by connection {
            private val defaultSchema = schema

            init {
                schema = "bc"
            }

            override fun close() {
                try {
                    schema = defaultSchema
                } finally {
                    connection.close()
                }
            }
        }
    }
}
