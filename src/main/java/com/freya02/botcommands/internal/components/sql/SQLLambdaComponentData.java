package com.freya02.botcommands.internal.components.sql;

import com.freya02.botcommands.api.components.ComponentType;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.api.components.builder.LambdaComponentTimeoutInfo;
import com.freya02.botcommands.internal.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLLambdaComponentData extends SQLComponentData {
	private final long handlerId;

	private SQLLambdaComponentData(String componentId, long groupId, boolean oneUse, InteractionConstraints interactionConstraints, long expirationTimestamp, long handlerId) {
		super(componentId, groupId, oneUse, interactionConstraints, expirationTimestamp);

		this.handlerId = handlerId;
	}

	@Nullable
	public static SQLLambdaComponentData read(Connection con, String componentId) throws SQLException {
		try (PreparedStatement preparedStatement = con.prepareStatement(
				"select * from lambdacomponentdata join componentdata using(componentid) where componentid = ? limit 1;"
		)) {
			preparedStatement.setString(1, componentId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return new SQLLambdaComponentData(
							componentId,
							resultSet.getLong("groupId"),
							resultSet.getBoolean("oneUse"),
							InteractionConstraints.fromJson(resultSet.getString("constraints")),
							resultSet.getLong("expirationTimestamp"),
							resultSet.getLong("handlerId")
					);
				} else {
					return null;
				}
			}
		}
	}

	public static SQLLambdaCreateResult create(Connection con, ComponentType type, boolean oneUse, InteractionConstraints constraints, LambdaComponentTimeoutInfo timeout) throws SQLException {
		SQLException lastEx = null;

		for (int i = 0; i < 10; i++) {
			final long timeoutMillis = timeout.toMillis();

			String randomId = Utils.randomId(64);

			try (PreparedStatement preparedStatement = con.prepareStatement(
					"insert into componentdata (type, componentid, oneuse, constraints, expirationtimestamp) values (?, ?, ?, ?, ?);"
			)) {
				preparedStatement.setInt(1, type.getKey());
				preparedStatement.setString(2, randomId);
				preparedStatement.setBoolean(3, oneUse);
				preparedStatement.setString(4, constraints.toJson());
				preparedStatement.setLong(5, timeoutMillis == 0 ? 0 : System.currentTimeMillis() + timeoutMillis);

				preparedStatement.execute();

				try (PreparedStatement preparedStatement1 = con.prepareStatement("insert into lambdacomponentdata (componentid) values (?) returning handlerid;")) {
					preparedStatement1.setString(1, randomId);

					try (ResultSet resultSet = preparedStatement1.executeQuery()) {
						if (resultSet.next()) {
							return new SQLLambdaCreateResult(randomId, resultSet.getLong("handlerId"));
						} else {
							throw new IllegalStateException("Lambda component insert into didn't return the handler id");
						}
					}
				}
			} catch (SQLException ex) {
				//ID already exists

				lastEx = ex;
			}
		}

		throw new SQLException("Could not insert a random component ID after 10 tries, maybe the database is full of IDs ?", lastEx);
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
