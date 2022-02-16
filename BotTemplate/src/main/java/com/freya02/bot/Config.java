package com.freya02.bot;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//You can add more fields in this class, if your input json matches the structure
//You will need a valid Config.json in the package com.freya02.bot for this to work
public class Config {
	@SuppressWarnings("unused") private String token;
	@SuppressWarnings("unused") private long ownerId;
	@SuppressWarnings("unused") private String prefix;
	@SuppressWarnings("unused") private DBConfig dbConfig;

	/**
	 * Returns the configuration object for this bot
	 *
	 * @return The config
	 * @throws IOException if the config JSON could not be read
	 */
	public static Config readConfig() throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(Path.of("Config.json"))) {
			return new Gson().fromJson(reader, Config.class);
		} catch (IOException e) {
			throw new IOException("""
					Config.json was not found in the root folder (of the project), did you forget to put it ?
					Example structure:

					{
						"token": "[your_bot_token_here]"
					}
					""", e);
		}
	}

	public String getToken() {
		return token;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public String getPrefix() {
		return prefix;
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
