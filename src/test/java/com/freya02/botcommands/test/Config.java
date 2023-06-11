package com.freya02.botcommands.test;

import com.freya02.botcommands.api.core.service.annotations.BService;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Config {
	@SuppressWarnings("unused") private String token;
	@SuppressWarnings("unused") private long ownerId;
	@SuppressWarnings("unused") private long testGuildId;
	@SuppressWarnings("unused") private String prefix;
	@SuppressWarnings("unused") private DBConfig dbConfig;

	/**
	 * Returns the configuration object for this bot
	 *
	 * @return The config
	 * @throws IOException if the config JSON could not be read
	 */
	public static Config readConfig() throws IOException {
		final InputStream stream = Config.class.getResourceAsStream("Config.json");
		if (stream == null) {
			throw new IOException("Config.json was not found in the current package, did you forget to put it ?");
		}

		try (InputStreamReader reader = new InputStreamReader(stream)) {
			return new Gson().fromJson(reader, Config.class);
		}
	}

	public String getToken() {
		return token;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public long getTestGuildId() {
		return testGuildId;
	}

	public String getPrefix() {
		return prefix;
	}

	public DBConfig getDbConfig() {
		return dbConfig;
	}

	@BService
	public static Config getInstance() throws IOException {
		return readConfig();
	}

	public static class DBConfig {
		@SuppressWarnings("unused") private String serverName, user, password, dbName;
		@SuppressWarnings("unused") private int portNumber;

		public String getServerName() {
			return serverName;
		}

		public String getUser() {
			return user;
		}

		public String getPassword() {
			return password;
		}

		public String getDbName() {
			return dbName;
		}

		public int getPortNumber() {
			return portNumber;
		}
	}
}
