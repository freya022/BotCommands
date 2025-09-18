package io.github.freya022.botcommands.api;

import io.github.freya022.botcommands.api.core.Logging;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Properties;

public class BCInfo {

    public static final Instant BUILD_TIME;
    public static final String VERSION_MAJOR;
    public static final String VERSION_MINOR;
    public static final String VERSION_REVISION;
    public static final String VERSION_CLASSIFIER;
    public static final String GITHUB = "https://github.com/freya022/BotCommands";
    /** May be "null", may also be a full commit hash in Jitpack builds */
    public static final String BRANCH_NAME;
    /** May be "null" */
    public static final String COMMIT_HASH;
    public static final String BUILD_JDA_VERSION;

    @SuppressWarnings("ConstantConditions")
    public static final String VERSION;

    static {
        final Properties properties = loadProperties();

        BUILD_TIME = Instant.ofEpochMilli(Long.parseLong(properties.getProperty("BUILD_TIME", "0")));
        VERSION_MAJOR = properties.getProperty("VERSION_MAJOR", "<major>");
        VERSION_MINOR = properties.getProperty("VERSION_MINOR", "<minor>");
        VERSION_REVISION = properties.getProperty("VERSION_REVISION", "<revision>");
        VERSION_CLASSIFIER = properties.getProperty("VERSION_CLASSIFIER", "<classifier>");
        BRANCH_NAME = properties.getProperty("BRANCH_NAME", "<branch>");
        COMMIT_HASH = properties.getProperty("COMMIT_HASH", "<commit>");
        BUILD_JDA_VERSION = properties.getProperty("BUILD_JDA_VERSION", "<JDA_version>");
        VERSION = "%s.%s.%s%s%s".formatted(VERSION_MAJOR, VERSION_MINOR, VERSION_REVISION,
                VERSION_CLASSIFIER.equals("null") ? "" : "-" + VERSION_CLASSIFIER,
                COMMIT_HASH.equals("null") ? "" : "_" + COMMIT_HASH);
    }

    @Nonnull
    private static Properties loadProperties() {
        final Properties properties = new Properties(8);
        try (InputStream stream = BCInfo.class.getResourceAsStream("/BCInfo.properties")) {
            if (stream == null) throw new IOException("Unable to find BCInfo.properties");
            try (InputStreamReader reader = new InputStreamReader(stream)) {
                properties.load(reader);
            }
        } catch (Exception e) {
            Logging.getLogger().error("Could not load properties file!", e);
        }
        return properties;
    }
}
