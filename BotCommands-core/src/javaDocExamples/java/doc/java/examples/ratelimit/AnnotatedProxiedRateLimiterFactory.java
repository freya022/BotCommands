package doc.java.examples.ratelimit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.freya022.botcommands.api.commands.ratelimit.AnnotatedRateLimiterFactory;
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope;
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter;
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import org.jspecify.annotations.NullMarked;

@BService
@NullMarked
public class AnnotatedProxiedRateLimiterFactory implements AnnotatedRateLimiterFactory {
    private final ProxyManager<String> proxyManager;

    public AnnotatedProxiedRateLimiterFactory(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    public RateLimiter create(RateLimitScope scope, BucketConfigurationSupplier configurationSupplier, boolean deleteOnRefill) {
        return RateLimiter.createDefaultProxied(scope, proxyManager, configurationSupplier, deleteOnRefill);
    }
}
