package doc.kotlin.examples.ratelimit

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.postgresql.Bucket4jPostgreSQL
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.test.switches.TestLanguage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.sql.PreparedStatement
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Configuration
@BService
open class ProxyManagerProvider {
    @TestLanguage(TestLanguage.Language.KOTLIN)
    @Bean
    @BService
    open fun proxyManager(hikariSourceSupplier: HikariSourceSupplier): ProxyManager<String> {
        // Create a proxy to manager buckets, persisting with PostgreSQL,
        // see https://bucket4j.com/8.14.0/toc.html#postgresqlselectforupdatebasedproxymanager
        return Bucket4jPostgreSQL.selectForUpdateBasedBuilder(hikariSourceSupplier.source)
            // Bucket expiration, needs to be triggered manually,
            // see https://bucket4j.com/8.14.0/toc.html#expiration-policy
            .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(1.minutes.toJavaDuration()))
            // RateLimiter#createDefaultProxied uses a String key
            .primaryKeyMapper(PreparedStatement::setString)
            .build()
    }
}
