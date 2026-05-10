package doc.java.examples.ratelimit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.api.ratelimit.AnnotatedRateLimiterFactory;
import io.github.freya022.botcommands.api.ratelimit.RateLimitScope;
import io.github.freya022.botcommands.api.ratelimit.RateLimiter;
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketConfigurationSupplier;
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
