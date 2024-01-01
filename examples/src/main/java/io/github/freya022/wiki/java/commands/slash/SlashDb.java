package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.core.Logging;
import io.github.freya022.botcommands.api.core.db.BlockingDatabase;
import io.github.freya022.botcommands.api.core.db.DBResult;
import io.github.freya022.botcommands.api.core.service.annotations.ServiceName;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@WikiLanguage(WikiLanguage.Language.JAVA)
@ServiceName("slashDbJava")
@Command
public class SlashDb extends ApplicationCommand {
    private static final Logger LOGGER = Logging.getLogger();

    private final BlockingDatabase database;

    public SlashDb(BlockingDatabase database) {
        this.database = database;
    }

    @JDASlashCommand(name = "db")
    public void onSlashDb(GuildSlashEvent event) throws SQLException {
        //TODO remove object array on alpha.10
        //TODO use stream on alpha.10
        // --8<-- [start:db_return_value-java]
        final long tagCount = database.withStatement("SELECT count(*) FROM tag", statement -> {
            return statement.executeQuery(new Object[0]).read() //Read a single row
                    .getLong(1); // Indexes start at 1
        });
        // --8<-- [end:db_return_value-java]

        // --8<-- [start:db_return_rows-java]
        final List<String> tagNames = database.withStatement("SELECT name FROM tag", statement -> {
            // Reads all rows and convert them to strings
            List<String> tagNamesTmp = new ArrayList<>();
            for (DBResult result : statement.executeQuery(new Object[0])) {
                tagNamesTmp.add(result.getString("name"));
            }
            return tagNamesTmp;
        });
        // --8<-- [end:db_return_rows-java]

        try {
            // --8<-- [start:db_transaction-java]
            database.withTransaction(transaction -> {
                // This should not be in the database since the next query will fail, thus reverting the transaction
                transaction.withStatement("INSERT INTO tag (name, content) VALUES ('should_not_be_here', 'should not be here')", statement -> {
                    statement.executeUpdate();
                    return null;
                });
                // This will raise an exception as the name has a constraint matching ^[\w-]+$ (spaces aren't allowed, for example)
                transaction.withStatement("INSERT INTO tag (name, content) VALUES ('invalid name', 'foo')", statement -> {
                    statement.executeUpdate();
                    return null;
                });
                return null;
            });
            // --8<-- [end:db_transaction-java]

            throw new IllegalStateException("Should not reach here!");
        } catch (Exception e) {
            if (e instanceof IllegalStateException) throw e;
            LOGGER.trace("Expected exception", e);
        }

        // --8<-- [start:db_generated_keys-java]
        final Instant createdAt = database.withStatement(
                "INSERT INTO tag (name, content) VALUES ('new_tag', 'new content')",
                new String[]{"created_at"}, // This is required as this is a generated column
                statement -> {
                    return statement.executeReturningUpdate() // executeUpdate() + getGeneratedKeys()
                            .read() //Read a single row
                            .getTimestamp("created_at").toInstant();
                });
        // --8<-- [end:db_generated_keys-java]

        event.replyFormat("""
                        Tag count: %s
                        Tag names: %s
                        New tag creation date: %s
                        """, tagCount, tagNames, TimeFormat.DATE_TIME_LONG.format(createdAt))
                .setEphemeral(true)
                .queue();
    }
}
