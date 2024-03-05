package io.github.freya022.wiki.java;

import io.github.freya022.wiki.config.Config;
import io.github.freya022.botcommands.api.core.JDAService;
import io.github.freya022.botcommands.api.core.events.BReadyEvent;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.wiki.switches.wiki.WikiLanguage;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@WikiLanguage(WikiLanguage.Language.JAVA)
// --8<-- [start:jdaservice-java]
@BService
public class Bot extends JDAService {
    private final Config config;

    public Bot(Config config) {
        this.config = config;
    }

    @NotNull
    @Override
    public Set<CacheFlag> getCacheFlags() {
        return Set.of();
    }

    @NotNull
    @Override
    public Set<GatewayIntent> getIntents() {
        return getDefaultIntents();
    }

    @Override
    public void createJDA(@NotNull BReadyEvent event, @NotNull IEventManager eventManager) {
        JDABuilder.createLight(config.getToken(), getIntents())
                .enableCache(getCacheFlags())
                .setActivity(Activity.customStatus("In Java with ❤️"))
                .setEventManager(eventManager)
                .build();
    }
}
// --8<-- [end:jdaservice-java]
