package io.github.freya022.bot.config;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.core.db.HikariSourceSupplier;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

// Interfaced service used to retrieve an SQL Connection
@BService
public class DatabaseSource implements HikariSourceSupplier {
    private static final Logger LOGGER = Logging.getLogger();

    private final HikariDataSource source;

    public DatabaseSource(Config config) {
        final var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getDatabaseConfig().getUrl());
        hikariConfig.setUsername(config.getDatabaseConfig().getUser());
        hikariConfig.setPassword(config.getDatabaseConfig().getPassword());

        source = new HikariDataSource(hikariConfig);

        //Migrate BC tables
        createFlyway("bc", "bc_database_scripts").migrate();

        //You can use the same function for your database,
        // you just have to change the schema and scripts location

        LOGGER.info("Created database source");
    }

    private Flyway createFlyway(String schema, String scriptsLocation) {
        return Flyway.configure()
                .dataSource(source)
                .schemas(schema)
                .locations(scriptsLocation)
                .validateMigrationNaming(true)
                .loggers("slf4j")
                .load();
    }

    @NotNull
    @Override
    public HikariDataSource getSource() {
        return source;
    }
}
