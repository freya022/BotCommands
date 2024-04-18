package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.core.db.BlockingDatabase;
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Command
@RequiresDatabase
public class SlashDbJava extends ApplicationCommand {
    @JDASlashCommand(name = "java_db")
    public void onSlashJavaDb(GuildSlashEvent event, BlockingDatabase database) throws SQLException {
        final String v = database.withTransaction(transaction -> {
            return transaction.withStatement("select version from bc.bc_version", statement -> {
                final ResultSet set = statement.executeQuery();
                set.next();
                return set.getString("version");
            });
        });

        final String v2 = database.withStatement("select version from bc.bc_version", statement -> {
            final ResultSet set = statement.executeQuery();
            set.next();
            return set.getString("version");
        });

        final String v3;
        try (Connection connection = database.fetchConnection();
             PreparedStatement statement = connection.prepareStatement("select version from bc.bc_version")) {
            final ResultSet set = statement.executeQuery();
            set.next();
            v3 = set.getString("version");
        }

        event.replyFormat("v: %s, v2: %s, v3: %s", v, v2, v3).setEphemeral(true).queue();
    }
}
