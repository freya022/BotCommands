package io.github.freya022.botcommands.api.ratelimit

import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketConfigurationSupplier

/**
 * Creates [RateLimiter] instances for annotations which creates rate limits,
 * the default implementation uses [RateLimiter.createDefault].
 *
 * ### Example
 * ```java
 * @BConfiguration
 * public class ProxyManagerProvider {
 *     @BService
 *     public static ProxyManager<String> proxyManager(HikariSourceSupplier hikariSourceSupplier) {
 *         // Create a proxy to manager buckets, persisting with PostgreSQL,
 *         // see https://bucket4j.com/8.14.0/toc.html#postgresqlselectforupdatebasedproxymanager
 *         return Bucket4jPostgreSQL.selectForUpdateBasedBuilder(hikariSourceSupplier.getSource())
 *                 // Bucket expiration, needs to be triggered manually,
 *                 // see https://bucket4j.com/8.14.0/toc.html#expiration-policy
 *                 .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)))
 *                 // RateLimiter#createDefaultProxied uses a String key
 *                 .primaryKeyMapper(PreparedStatement::setString)
 *                 .build();
 *     }
 * }
 * ```
 *
 * ```java
 * @BService
 * @NullMarked
 * public class AnnotatedProxiedRateLimiterFactory implements AnnotatedRateLimiterFactory {
 *     private final ProxyManager<String> proxyManager;
 *
 *     public AnnotatedProxiedRateLimiterFactory(ProxyManager<String> proxyManager) {
 *         this.proxyManager = proxyManager;
 *     }
 *
 *     @Override
 *     public RateLimiter create(RateLimitScope scope, BucketConfigurationSupplier configurationSupplier, boolean deleteOnRefill) {
 *         return RateLimiter.createDefaultProxied(scope, proxyManager, configurationSupplier, deleteOnRefill);
 *     }
 * }
 * ```
 */
@InterfacedService(acceptMultiple = false)
interface AnnotatedRateLimiterFactory {
    fun create(scope: RateLimitScope, configurationSupplier: BucketConfigurationSupplier, deleteOnRefill: Boolean): RateLimiter
}
