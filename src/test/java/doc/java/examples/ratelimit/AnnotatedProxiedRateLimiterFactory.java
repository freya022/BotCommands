package doc.java.examples.ratelimit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.freya022.botcommands.api.commands.ratelimit.AnnotatedRateLimiterFactory;
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope;
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter;
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@BService
public class AnnotatedProxiedRateLimiterFactory implements AnnotatedRateLimiterFactory {
    private final ProxyManager<BigDecimal> proxyManager;

    public AnnotatedProxiedRateLimiterFactory(ProxyManager<BigDecimal> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @NotNull
    @Override
    public RateLimiter create(@NotNull RateLimitScope scope, @NotNull BucketConfigurationSupplier configurationSupplier, boolean deleteOnRefill) {
        return RateLimiter.createDefaultProxied(scope, proxyManager, configurationSupplier, deleteOnRefill);
    }
}
