package io.github.freya022.botcommands.test;

import com.zaxxer.hikari.HikariDataSource;
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier;
import io.github.freya022.botcommands.api.core.service.annotations.BService;
import org.jetbrains.annotations.NotNull;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.SQLException;

@BService
public class TestDB implements HikariSourceSupplier {
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

	@NotNull
	@Override
	public HikariDataSource getSource() {
		return source;
	}
}
