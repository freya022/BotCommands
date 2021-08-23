package com.freya02.botcommands.components.internal.sql;

import com.freya02.botcommands.components.ComponentType;
import com.freya02.botcommands.internal.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlLambdaComponentData extends SqlComponentData {
	private final long handlerId;

	private SqlLambdaComponentData(String componentId, long groupId, boolean oneUse, long ownerId, long expirationTimestamp, long handlerId) {
		super(componentId, groupId, oneUse, ownerId, expirationTimestamp);

		this.handlerId = handlerId;
	}

	public static SqlLambdaComponentData read(Connection con, String componentId) throws SQLException {
		try (PreparedStatement preparedStatement = con.prepareStatement(
				"select * from lambdacomponentdata join componentdata using(componentid) where componentid = ?"
		)) {
			preparedStatement.setString(1, componentId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return new SqlLambdaComponentData(
							componentId,
							resultSet.getLong("groupId"),
							resultSet.getBoolean("oneUse"),
							resultSet.getLong("ownerId"),
							resultSet.getLong("expirationTimestamp"),
							resultSet.getLong("handlerId")
					);
				} else {
					return null;
				}
			}
		}
	}

	public static SqlLambdaCreateResult create(Connection con, ComponentType type, boolean oneUse, long ownerId, long timeoutMillis) throws SQLException {
		while (true) {
			String randomId = Utils.randomId(64);

			try (PreparedStatement preparedStatement = con.prepareStatement(
					"insert into componentdata (type, componentid, oneuse, ownerid, expirationtimestamp) values (?, ?, ?, ?, ?);"
			)) {
				preparedStatement.setInt(1, type.getKey());
				preparedStatement.setString(2, randomId);
				preparedStatement.setBoolean(3, oneUse);
				preparedStatement.setLong(4, ownerId);
				preparedStatement.setLong(5, timeoutMillis == 0 ? 0 : System.currentTimeMillis() + timeoutMillis);

				preparedStatement.execute();

				try (PreparedStatement preparedStatement1 = con.prepareStatement("insert into lambdacomponentdata (componentid) values (?) returning handlerid;")) {
					preparedStatement1.setString(1, randomId);

					try (ResultSet resultSet = preparedStatement1.executeQuery()) {
						if (resultSet.next()) {
							return new SqlLambdaCreateResult(randomId, resultSet.getLong("handlerId"));
						} else {
							throw new IllegalStateException("Lambda component insert into didn't return the handler id");
						}
					}
				}
			} catch (SQLException ignored) {
				//ID already exists
			}
		}
	}

	public long getHandlerId() {
		return handlerId;
	}

	@Override
	public String toString() {
		return "SqlLambdaComponentData{" +
				"handlerId=" + handlerId +
				"} " + super.toString();
	}
}
