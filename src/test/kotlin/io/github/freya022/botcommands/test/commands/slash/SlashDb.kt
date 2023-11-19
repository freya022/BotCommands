package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.preparedStatement
import io.github.freya022.botcommands.api.core.db.transactional
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies

@Command
@Dependencies(Database::class)
class SlashDb(private val database: Database) : ApplicationCommand() {
    @JDASlashCommand(name = "db")
    suspend fun onSlashDb(event: GuildSlashEvent) {
        val v: String = database.transactional {
            preparedStatement("select version from bc.bc_version") {
                executeQuery().read()["version"]
            }
        }

        val v2: String = database.preparedStatement("select version from bc.bc_version") {
            executeQuery().read()["version"]
        }

        val v3: String = database.fetchConnection().use { connection ->
            connection.prepareStatement("select version from bc.bc_version").use {
                val set = it.executeQuery()
                set.next()
                set.getString("version")
            }
        }

        event.reply_("v: $v, v2: $v2, v3: $v3", ephemeral = true).await()
    }
}