package com.freya02.botcommands.api.core.db;

import com.freya02.botcommands.api.core.config.BComponentsConfigBuilder;
import com.freya02.botcommands.api.core.config.BConfig;
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InjectedService;
import com.freya02.botcommands.api.core.service.annotations.InterfacedService;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

/**
 * This interface allows the framework to access a PostgreSQL database.
 *
 * <p>
 * <b>Usage</b>: Register your instance as a service with {@link BService}
 * or {@link BServiceConfigBuilder#getServiceAnnotations() any annotation that enables your class for dependency injection}.
 *
 * @see InterfacedService @InterfacedService
 * @see BComponentsConfigBuilder#setUseComponents(boolean)
 */
@InterfacedService(acceptMultiple = false)
@InjectedService(message = "A service implementing ConnectionSupplier and annotated with @BService " +
		"needs to be set in order to use the database")
public interface ConnectionSupplier {
	int getMaxConnections();

	/**
	 * Returns the duration until a thread/coroutine dump is attempted during a transaction.
	 *
	 * <p>This should be the same value as what HikariCP is using as the leak detection threshold.
	 *
	 * @see BConfig#getDumpLongTransactions()
	 */
	@NotNull
	default Duration getMaxTransactionDuration() { return Duration.ZERO; }

	@NotNull
	Connection getConnection() throws SQLException;
}
