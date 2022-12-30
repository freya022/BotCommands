package com.freya02.botcommands.test;

import com.freya02.botcommands.api.core.ServiceStart;
import com.freya02.botcommands.api.core.annotations.BService;
import com.freya02.botcommands.api.core.annotations.ServiceType;
import com.freya02.botcommands.api.core.db.ConnectionSupplier;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

@BService(start = ServiceStart.PRE_LOAD)
@ServiceType(type = ConnectionSupplier.class)
public class TestDB {
	private final HikariDataSource source = new HikariDataSource();

	public TestDB(Config config) {
		final Config.DBConfig dbConfig = config.getDbConfig();

		final PGSimpleDataSource pgSource = new PGSimpleDataSource();
		pgSource.setServerNames(new String[]{dbConfig.getServerName()});
		pgSource.setPortNumbers(new int[]{dbConfig.getPortNumber()});
		pgSource.setUser(dbConfig.getUser());
		pgSource.setPassword(dbConfig.getPassword());
		pgSource.setDatabaseName(dbConfig.getDbName());

		source.setDataSource(pgSource);
		source.setMaximumPoolSize(2);
		source.setLeakDetectionThreshold(2500);

		try {
			pgSource.getConnection().close();
		} catch (SQLException e) {
			throw new RuntimeException("Unable to test PGSQL connection", e);
		}
	}

	public Supplier<Connection> getConnectionSupplier() {
		return () -> {
			try {
				return source.getConnection();
			} catch (SQLException e) {
				throw new RuntimeException("Unable to get PGSQL connection", e);
			}
		};
	}

//	@Override
	public Connection getConnection() throws SQLException {
		return source.getConnection();
	}
}
