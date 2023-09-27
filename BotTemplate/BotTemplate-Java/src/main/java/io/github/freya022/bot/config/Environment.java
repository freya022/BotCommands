package io.github.freya022.bot.config;

import java.nio.file.Files;
import java.nio.file.Path;

public class Environment {
    /**
     * The mode is determined by checking if the
     * {@code dev-config} directory exists in the current directory.
     */
    public static final boolean IS_DEV = Files.exists(Path.of("dev-config"));

    /**
     * The folder where the data and configuration directories reside.
     * <br>This is the current <b>working directory</b>.
     */
    public static final Path FOLDER = Path.of("");

    public static final Path CONFIG_FOLDER = FOLDER.resolve(IS_DEV ? "dev-config" : "config");
    public static final Path LOGBACK_CONFIG_PATH = CONFIG_FOLDER.resolve(IS_DEV ? "logback-test.xml" : "logback.xml");
}
