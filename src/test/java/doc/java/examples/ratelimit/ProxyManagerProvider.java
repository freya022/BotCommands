package doc.java.examples.ratelimit;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.postgresql.Bucket4jPostgreSQL;
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier;
import io.github.freya022.botcommands.api.core.service.annotations.BConfiguration;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.PreparedStatement;
import java.time.Duration;

@Configuration
@BConfiguration
public class ProxyManagerProvider {
    @TestLanguage(TestLanguage.Language.JAVA)
    @Bean
    @BService
    public static ProxyManager<String> proxyManager(HikariSourceSupplier hikariSourceSupplier) {
        // Create a proxy to manager buckets, persisting with PostgreSQL,
        // see https://bucket4j.com/8.14.0/toc.html#postgresqlselectforupdatebasedproxymanager
        return Bucket4jPostgreSQL.selectForUpdateBasedBuilder(hikariSourceSupplier.getSource())
                // Bucket expiration, needs to be triggered manually,
                // see https://bucket4j.com/8.14.0/toc.html#expiration-policy
                .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1)))
                // RateLimiter#createDefaultProxied uses a String key
                .primaryKeyMapper(PreparedStatement::setString)
                .build();
    }
}
