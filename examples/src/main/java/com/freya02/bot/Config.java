package com.freya02.bot;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//You can add more fields in this class, if your input json matches the structure
//You will need a valid Config.json in the package com.freya02.bot for this to work
public class Config {
	@SuppressWarnings("unused") private String token;
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
			throw new IOException("Config.json was not found in the current package, did you forget to put it ?\n" +
					"Example structure:\n\n" +
					"{\n" +
					"\t\"token\": \"[your_bot_token_here]\"\n" +
					"}\n");
		}

		try (InputStreamReader reader = new InputStreamReader(stream)) {
			return new Gson().fromJson(reader, Config.class);
		}
	}

	public String getToken() {
		return token;
	}

	public DBConfig getDbConfig() {
		return dbConfig;
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
