package io.github.freya022.bot;

import ch.qos.logback.classic.ClassicConstants;
import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.core.BBuilder;
import io.github.freya022.bot.config.Config;
import io.github.freya022.bot.config.Environment;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.slf4j.Logger;

public class Main {
    private static final String MAIN_PACKAGE_NAME = "io.github.freya022.bot";
    private static final String BOT_NAME = "BotTemplate";

    public static void main(String[] args) {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.LOGBACK_CONFIG_PATH.toAbsolutePath().toString());
        final Logger logger = Logging.getLogger();
        logger.info("Loading logback configuration at {}", Environment.LOGBACK_CONFIG_PATH.toAbsolutePath());

        // I use hotswap agent in order to update my code without restarting the bot
        // Of course this only supports modifying existing code
        // Refer to https://github.com/HotswapProjects/HotswapAgent#readme on how to use hotswap

        try {
            final Config config = Config.getInstance();
            BBuilder.newBuilder(builder -> {
                if (Environment.IS_DEV) {
                    builder.setDisableExceptionsInDMs(true);
                    builder.setDisableAutocompleteCache(true);
                }

                builder.getOwnerIds().addAll(config.getOwnerIds());

                builder.addSearchPath(MAIN_PACKAGE_NAME);

                builder.textCommands(textCommands -> {
                    //Use ping as prefix if configured
                    textCommands.setUsePingAsPrefix(config.getPrefixes().contains("<ping>"));

                    for (String prefix : config.getPrefixes()) {
                        if (prefix.equals("<ping>")) continue;
                        textCommands.getPrefixes().add(prefix);
                    }
                });

                builder.applicationCommands(applicationCommands -> {
                    // Check command updates based on Discord's commands.
                    // This is only useful during development,
                    // as you can develop on multiple machines. (but not simultaneously!)
                    // Using this in production is only going to waste API requests.
                    applicationCommands.setOnlineAppCommandCheckEnabled(Environment.IS_DEV);

                    // Guilds in which @Test commands will be inserted
                    applicationCommands.getTestGuildIds().addAll(config.getTestGuildIds());

                    // Add french localization for application commands
                    applicationCommands.addLocalizations("Commands", DiscordLocale.FRENCH);
                });

                builder.components(components -> {
                    components.setUseComponents(true);
                });
            });

            // There is no JDABuilder going on here, it's taken care of in Bot

            logger.info("Loaded bot");
        } catch (Exception e) {
            logger.error("Unable to start the bot", e);
            System.exit(1);
        }
    }
}
