package com.freya02.botcommands.api.core.db;

import com.freya02.botcommands.api.core.annotations.BService;
import com.freya02.botcommands.api.core.annotations.InjectedService;
import com.freya02.botcommands.api.core.annotations.ServiceType;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This interface allows the framework to access a PostgreSQL database.
 *
 * <p><b>Requirements:</b>
 * <ul>
 *     <li>This interface is implemented on at most 1 class</li>
 *     <li>The class needs to be annotated with {@code @BService(start = ServiceStart.PRE_LOAD)}</li>
 *     <li>The class needs to be annotated with {@code @ServiceType(type = ConnectionSupplier.class)}</li>
 * </ul>
 *
 * @see BService
 * @see ServiceType
 * @see InjectedService
 */
@InjectedService(message = "A service implementing ConnectionSupplier and annotated with @BService(start = ServiceStart.PRE_LOAD) and @ServiceType(type = ConnectionSupplier.class) " +
		"needs to be set in order to use the database")
public interface ConnectionSupplier {
	Connection getConnection() throws SQLException;
}
