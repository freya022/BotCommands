package io.github.freya022.bot.config;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.google.gson.Gson;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Config {
    private static final Logger LOGGER = Logging.getLogger();
    private static final Path CONFIG_FILE_PATH = Environment.CONFIG_FOLDER.resolve("config.json");

    private String token;
    private List<Long> ownerIds;
    private List<String> prefixes;
    private List<Long> testGuildIds;
    private DatabaseConfig databaseConfig;

    public String getToken() {
        return token;
    }

    public List<Long> getOwnerIds() {
        return ownerIds;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public List<Long> getTestGuildIds() {
        return testGuildIds;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    @BService
    public static Config getInstance() throws IOException {
        LOGGER.info("Loading configuration at {}", CONFIG_FILE_PATH.toAbsolutePath());

        return new Gson().fromJson(Files.readString(CONFIG_FILE_PATH), Config.class);
    }
}
