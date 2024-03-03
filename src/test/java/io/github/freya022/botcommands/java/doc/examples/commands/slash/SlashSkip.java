package io.github.freya022.botcommands.java.doc.examples.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory;
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitManager;
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@TestLanguage(TestLanguage.Language.JAVA)
@Command
public class SlashSkip implements RateLimitProvider {
    private static final String SKIP_RATE_LIMIT_NAME = "SlashSkip: skip";

    @JDASlashCommand(name = "skip")
    @RateLimitReference(SKIP_RATE_LIMIT_NAME)
    public void onSlashSkip(GuildSlashEvent event) {
        event.reply("Skipped").setEphemeral(true).flatMap(InteractionHook::deleteOriginal).queue();
    }

    @Override
    public void declareRateLimit(@NotNull RateLimitManager manager) {
        final var bucketFactory = BucketFactory.spikeProtected(
                /* Capacity */ 5,
                /* Duration */ Duration.ofMinutes(1),
                /* Spike capacity */ 2,
                /* Spike duration */ Duration.ofSeconds(5)
        );
        manager.rateLimit(SKIP_RATE_LIMIT_NAME, bucketFactory);
    }
}
