package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.preparedStatement
import io.github.freya022.botcommands.api.core.db.transactional
import io.github.freya022.wiki.switches.wiki.WikiLanguage
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.utils.TimeFormat
import java.time.Instant

private val logger = KotlinLogging.logger { }

@WikiLanguage(WikiLanguage.Language.KOTLIN)
@Command
class SlashDb(private val database: Database) : ApplicationCommand() {
    @JDASlashCommand(name = "db")
    suspend fun onSlash(event: GuildSlashEvent) {
        //TODO capitalize

        // --8<-- [start:db_return_value-kotlin]
        val tagCount: Long = database.preparedStatement("select count(*) from tag") {
            val dbResult = executeQuery().read() //Read a single row
            dbResult[1] // Indexes start at 1
        }
        // --8<-- [end:db_return_value-kotlin]

        // --8<-- [start:db_return_rows-kotlin]
        val tagNames: List<String> = database.preparedStatement("select name from tag") {
            // Reads all rows and convert them to strings (type inference with List<String>)
            executeQuery().map { it["name"] }
        }
        // --8<-- [end:db_return_rows-kotlin]

        try {
            // --8<-- [start:db_transaction-kotlin]
            database.transactional {
                // This should not be in the database since the next query will fail, thus reverting the transaction
                preparedStatement("insert into tag (name, content) values ('should_not_be_here', 'should not be here')") {
                    executeUpdate()
                }
                // This will raise an exception as the name has a constraint matching ^[\w-]+$ (spaces aren't allowed, for example)
                preparedStatement("insert into tag (name, content) values ('invalid name', 'foo')") {
                    executeUpdate()
                }
            }
            // --8<-- [end:db_transaction-kotlin]

            throw IllegalStateException("Should not reach here!")
        } catch (e: Exception) {
            if (e is IllegalStateException) throw e
            logger.trace(e) { "Expected exception" }
        }

        // --8<-- [start:db_generated_keys-kotlin]
        val createdAt: Instant = database.preparedStatement(
            "insert into tag (name, content) values ('new_tag', 'new content')",
            columnNames = arrayOf("created_at") // This is required as this is a generated column
        ) {
            executeReturningUpdate() // executeUpdate() + getGeneratedKeys()
                .read() //Read a single row
                .getTimestamp("created_at").toInstant()
        }
        // --8<-- [end:db_generated_keys-kotlin]

        event.reply_("""
            Tag count: $tagCount
            Tag names: $tagNames
            New tag creation date: ${TimeFormat.DATE_TIME_LONG.format(createdAt)}
        """.trimIndent(), ephemeral = true).await()
    }
}