package io.github.freya022.botcommands.internal.commands.application.cache

import io.github.freya022.botcommands.api.core.db.preparedStatement
import io.github.freya022.botcommands.internal.core.db.InternalDatabase
import net.dv8tion.jda.api.entities.Guild

internal class DatabaseApplicationCommandsCache internal constructor(
    private val guild: Guild?,
    private val database: InternalDatabase,
    private val applicationId: Long,
) : ApplicationCommandsCache {

    override suspend fun tryRead(): ApplicationCommandsData {
        database.preparedStatement(
            """
                select data, metadata
                from application_commands_cache
                where application_id = ?
                  and guild_id is not distinct from ?
            """.trimIndent(),
            readOnly = true
        ) {
            val row = executeQuery(applicationId, guild?.idLong).readOrNull()
                ?: return ApplicationCommandsData(null, null)
            return ApplicationCommandsData(row["data"], row["metadata"])
        }
    }

    override suspend fun write(commandBytes: ByteArray, metadataBytes: ByteArray) {
        database.preparedStatement(
            """
                insert into application_commands_cache (application_id, guild_id, data, metadata)
                values (?, ?, ?, ?)
                on conflict(application_id, guild_id) do update set data     = excluded.data,
                                                                    metadata = excluded.metadata
            """.trimIndent()
        ) {
            val commands = commandBytes.decodeToString()
            val metadata = metadataBytes.decodeToString()
            executeUpdate(applicationId, guild?.idLong, commands, metadata)
        }
    }

    override fun toString(): String {
        return "DatabaseApplicationCommandsCache(guild=${guild?.id})"
    }
}