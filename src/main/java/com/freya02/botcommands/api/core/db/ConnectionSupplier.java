package com.freya02.botcommands.api.core.db;

import com.freya02.botcommands.api.core.config.BComponentsConfigBuilder;
import com.freya02.botcommands.api.core.service.annotations.BService;
import com.freya02.botcommands.api.core.service.annotations.InjectedService;
import com.freya02.botcommands.api.core.service.annotations.ServiceType;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This interface allows the framework to access a PostgreSQL database.
 *
 * <p><b>Requirements:</b>
 * <ul>
 *     <li>This interface is implemented on at most 1 class</li>
 *     <li>The class needs to be annotated with {@code @BService}</li>
 *     <li>The class needs to be annotated with {@code @ServiceType(ConnectionSupplier.class)}</li>
 * </ul>
 *
 * @see BService
 * @see ServiceType
 * @see InjectedService
 * @see BComponentsConfigBuilder#setUseComponents(boolean)
 */
@InjectedService(message = "A service implementing ConnectionSupplier and annotated with @BService and @ServiceType(ConnectionSupplier.class) " +
		"needs to be set in order to use the database")
public interface ConnectionSupplier {
	int getMaxConnections();

	Connection getConnection() throws SQLException;
}
