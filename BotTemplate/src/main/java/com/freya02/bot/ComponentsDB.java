package com.freya02.bot;

import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ComponentsDB {
	private final HikariDataSource source;

	public ComponentsDB(Config config) throws SQLException {
		final Config.DBConfig dbConfig = config.getDbConfig();

		final PGSimpleDataSource pgSource = new PGSimpleDataSource();
		pgSource.setServerNames(new String[]{dbConfig.getServerName()});
		pgSource.setPortNumbers(new int[]{dbConfig.getPortNumber()});
		pgSource.setUser(dbConfig.getUser());
		pgSource.setPassword(dbConfig.getPassword());
		pgSource.setDatabaseName(dbConfig.getDbName());

		source = new HikariDataSource();
		source.setDataSource(pgSource);

		source.getConnection().close();
	}

	public Connection getConnection() {
		try {
			return source.getConnection();
		} catch (SQLException e) {
			throw new RuntimeException("Unable to get a SQL connection", e);
		}
	}
}