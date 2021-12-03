package com.freya02.botcommands.internal.components.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freya02.botcommands.api.components.ComponentType;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.components.builder.PersistentComponentTimeoutInfo;
import com.freya02.botcommands.internal.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class SQLPersistentComponentData extends SQLComponentData {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final String handlerName;
	private final String[] args;

	private SQLPersistentComponentData(String componentId, long groupId, boolean oneUse, InteractionConstraints interactionConstraints, long expirationTimestamp, String handlerName, String[] args) {
		super(componentId, groupId, oneUse, interactionConstraints, expirationTimestamp);

		this.handlerName = handlerName;
		this.args = args;
	}

	private static String writeStringArray(String[] strings) {
		try {
			return MAPPER.writeValueAsString(strings);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Unable to serialize data: " + Arrays.toString(strings), e);
		}
	}

	private static String[] readStringArray(String json) {
		try {
			return MAPPER.readValue(json, String[].class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Unable to deserialize data: " + json, e);
		}
	}

	@Nullable
	public static SQLPersistentComponentData read(Connection con, String componentId) throws SQLException {
		try (PreparedStatement preparedStatement = con.prepareStatement(
				"select * from persistentcomponentdata join componentdata using(componentid) where componentid = ?"
		)) {
			preparedStatement.setString(1, componentId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return new SQLPersistentComponentData(
							componentId,
							resultSet.getLong("groupId"),
							resultSet.getBoolean("oneUse"),
							InteractionConstraints.fromJson(resultSet.getString("constraints")),
							resultSet.getLong("expirationTimestamp"),
							resultSet.getString("handlerName"),
							readStringArray(resultSet.getString("args"))
					);
				} else {
					return null;
				}
			}
		}
	}

	public static String create(Connection con, ComponentType type, boolean oneUse, InteractionConstraints constraints, PersistentComponentTimeoutInfo timeout, String handlerName, String[] args) throws SQLException {
		SQLException lastEx = null;

		for (int i = 0; i < 10; i++) {
			long timeoutMillis = timeout.toMillis();

			String randomId = Utils.randomId(64);

			try (PreparedStatement preparedStatement = con.prepareStatement(
					"insert into componentdata (type, componentid, oneuse, constraints, expirationtimestamp) values (?, ?, ?, ?, ?);\n" +
							"insert into persistentcomponentdata (componentid, handlername, args) values (?, ?, ?);"
			)) {
				preparedStatement.setInt(1, type.getKey());
				preparedStatement.setString(2, randomId);
				preparedStatement.setBoolean(3, oneUse);
				preparedStatement.setString(4, constraints.toJson());
				preparedStatement.setLong(5, timeoutMillis == 0 ? 0 : System.currentTimeMillis() + timeoutMillis);

				preparedStatement.setString(6, randomId);
				preparedStatement.setString(7, handlerName);
				preparedStatement.setString(8, writeStringArray(args));

				preparedStatement.execute();

				return randomId;
			} catch (SQLException ex) {
				//ID already exists

				lastEx = ex;
			}
		}

		throw new SQLException("Could not insert a random component ID after 10 tries, maybe the database is full of IDs ?", lastEx);
	}

	public String getHandlerName() {
		return handlerName;
	}

	public String[] getArgs() {
		return args;
	}

	@Override
	public String toString() {
		return "SqlPersistentComponentData{" +
				"handlerName='" + handlerName + '\'' +
				", args=" + Arrays.toString(args) +
				"} " + super.toString();
	}
}
