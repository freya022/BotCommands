package io.github.freya022.bot.config;

import java.nio.file.Files;
import java.nio.file.Path;

public class Data {
    /**
     * Where your bot can write data if needed
     */
    public static final Path FOLDER = Environment.FOLDER.resolve(Environment.IS_DEV ? "dev-data" : "data");

    /**
     * Checks whether the path exists, throwing if not.
     */
    private static Path validate(Path path, String description) {
        if (Files.notExists(path))
            throw new IllegalArgumentException("%s at %s does not exist.".formatted(description, path.toAbsolutePath()));
        return path;
    }
}
