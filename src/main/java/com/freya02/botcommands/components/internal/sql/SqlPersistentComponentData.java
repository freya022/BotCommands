package com.freya02.botcommands.components.internal.sql;

import com.freya02.botcommands.components.ComponentType;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class SqlPersistentComponentData extends SqlComponentData {
	private static final Gson GSON = new Gson();

	private final String handlerName;
	private final String[] args;

	private SqlPersistentComponentData(String componentId, long groupId, boolean oneUse, long ownerId, long expirationTimestamp, String handlerName, String[] args) {
		super(componentId, groupId, oneUse, ownerId, expirationTimestamp);

		this.handlerName = handlerName;
		this.args = args;
	}

	public static SqlPersistentComponentData read(Connection con, String componentId) throws SQLException {
		try (PreparedStatement preparedStatement = con.prepareStatement(
				"select * from persistentcomponentdata join componentdata using(componentid) where componentid = ?"
		)) {
			preparedStatement.setString(1, componentId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return new SqlPersistentComponentData(
							componentId,
							resultSet.getLong("groupId"),
							resultSet.getBoolean("oneUse"),
							resultSet.getLong("ownerId"),
							resultSet.getLong("expirationTimestamp"),
							resultSet.getString("handlerName"),
							GSON.fromJson(resultSet.getString("args"), String[].class)
					);
				} else {
					return null;
				}
			}
		}
	}

	public static String create(Connection con, ComponentType type, boolean oneUse, long ownerId, long timeoutMillis, String handlerName, String[] args) throws SQLException {
		String randomId = getRandomId(con);

		try (PreparedStatement preparedStatement = con.prepareStatement(
				"insert into componentdata (type, componentid, oneuse, ownerid, expirationtimestamp) values (?, ?, ?, ?, ?);\n" +
						"insert into persistentcomponentdata (componentid, handlername, args) values (?, ?, ?);"
		)) {
			preparedStatement.setInt(1, type.getKey());
			preparedStatement.setString(2, randomId);
			preparedStatement.setBoolean(3, oneUse);
			preparedStatement.setLong(4, ownerId);
			preparedStatement.setLong(5, timeoutMillis == 0 ? 0 : System.currentTimeMillis() + timeoutMillis);

			preparedStatement.setString(6, randomId);
			preparedStatement.setString(7, handlerName);
			preparedStatement.setString(8, GSON.toJson(args));

			preparedStatement.execute();
		}

		return randomId;
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
