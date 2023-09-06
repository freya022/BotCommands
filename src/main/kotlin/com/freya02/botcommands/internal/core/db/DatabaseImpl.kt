package com.freya02.botcommands.internal.core.db

import com.freya02.botcommands.api.core.config.BConfig
import com.freya02.botcommands.api.core.db.ConnectionSupplier
import com.freya02.botcommands.api.core.db.Database
import com.freya02.botcommands.api.core.db.preparedStatement
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.Dependencies
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.sql.Connection

private const val latestVersion = "3.0.0-alpha.7" // Change in base migration script too

@BService
@ServiceType(Database::class)
@Dependencies(ConnectionSupplier::class)
internal class DatabaseImpl internal constructor(
    private val connectionSupplier: ConnectionSupplier,
    override val config: BConfig
) : Database {
    //Prevents deadlock when a paused coroutine holds a Connection,
    // but cannot be resumed and freed because of the coroutine scope being full (from another component event)
    private val semaphore = Semaphore(connectionSupplier.maxConnections)

    private lateinit var baseSchema: String

    init {
        runBlocking {
            preparedStatement("select version from bc.bc_version", readOnly = true) {
                val rs = executeQuery().readOrNull() ?:
                    throw IllegalStateException("No version found, please create the BotCommands tables with the migration scripts in 'bc_database_scripts', in the resources folder (you can also use Flyway for example)")

                val version = rs.getString("version")

                if (version != latestVersion) {
                    throw IllegalStateException("The current database version is '$version' and the version needed is '$latestVersion', please upgrade/downgrade the database with the help of the migration scripts, don't forget about backups if needed")
                }
            }
        }
    }

    override suspend fun fetchConnection(readOnly: Boolean): Connection = semaphore.withPermit {
        connectionSupplier.connection.also {
            if (!::baseSchema.isInitialized) {
                baseSchema = it.schema
            } else {
                // Reset schema as it isn't done by HikariCP
                // in situations where a schema isn't set on the connection pool
                it.schema = baseSchema
            }
            it.isReadOnly = readOnly
        }
    }
}