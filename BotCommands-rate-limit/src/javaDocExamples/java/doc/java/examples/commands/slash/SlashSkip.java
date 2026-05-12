package doc.java.examples.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.ratelimit.RateLimitScope;
import io.github.freya022.botcommands.api.ratelimit.RateLimiter;
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimitReference;
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketConfigurationSupplier;
import io.github.freya022.botcommands.api.ratelimit.bucket.Buckets;
import io.github.freya022.botcommands.api.ratelimit.declaration.RateLimitManager;
import io.github.freya022.botcommands.api.ratelimit.declaration.RateLimitProvider;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;

@Command
@NullMarked
public class SlashSkip implements RateLimitProvider {
    private static final String SKIP_RATE_LIMIT_NAME = "SlashSkip: skip";

    @JDASlashCommand(name = "skip")
    @RateLimitReference(SKIP_RATE_LIMIT_NAME)
    public void onSlashSkip(GuildSlashEvent event) {
        event.reply("Skipped").setEphemeral(true).flatMap(InteractionHook::deleteOriginal).queue();
    }

    @Override
    public void declareRateLimit(RateLimitManager manager) {
        final var bucketFactory = Buckets.createSpikeProtected(
                /* Capacity */ 5,
                /* Duration */ Duration.ofMinutes(1),
                /* Spike capacity */ 2,
                /* Spike duration */ Duration.ofSeconds(5)
        );
        manager.rateLimit(
                SKIP_RATE_LIMIT_NAME,
                RateLimiter.createDefault(RateLimitScope.USER, BucketConfigurationSupplier.constant(bucketFactory), /* deleteOnRefill */ true)
        );
    }
}
