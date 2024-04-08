package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.BCInfo
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.preparedStatement
import io.github.freya022.botcommands.api.core.db.transactional
import io.github.freya022.botcommands.api.core.db.withLogger
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.core.db.RequiresDatabase
import io.github.oshai.kotlinlogging.KotlinLogging

@Command
@Dependencies(Database::class)
@RequiresDatabase
class SlashDb(private val database: Database) : ApplicationCommand() {
    @JDASlashCommand(name = "db")
    suspend fun onSlashDb(event: GuildSlashEvent) {
        event.deferReply(true).queue()

        val v: String = database.transactional {
            preparedStatement("select version from bc.bc_version") {
                executeQuery().read()["version"]
            }
        }

        val v2: String = database.preparedStatement("select version from bc.bc_version") {
            executeQuery().read()["version"]
        }

        database.preparedStatement<String>("select version from bc.bc_version") {
            withLogger(KotlinLogging.loggerOf<BCInfo>())
            executeQuery().read()["version"]
        }

        val v3: String = database.fetchConnection().use { connection ->
            connection.prepareStatement("select version from bc.bc_version").use {
                val set = it.executeQuery()
                set.next()
                set.getString("version")
            }
        }

        event.hook.sendMessage("v: $v, v2: $v2, v3: $v3").await()
    }
}