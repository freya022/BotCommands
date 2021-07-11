package com.freya02.botcommands.components.internal.sql;

import com.freya02.botcommands.Logging;
import com.freya02.botcommands.Utils;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class SqlComponentData {
	private static final Object ID_CREATE_LOCK = new Object();
	private static final Logger LOGGER = Logging.getLogger();

	private final String componentId;
	private final long groupId;
	private final boolean oneUse;
	private final long ownerId;
	private final long expirationTimestamp;

	public SqlComponentData(String componentId, long groupId, boolean oneUse, long ownerId, long expirationTimestamp) {
		this.componentId = componentId;
		this.groupId = groupId;
		this.oneUse = oneUse;
		this.ownerId = ownerId;
		this.expirationTimestamp = expirationTimestamp;
	}

	@Nonnull
	static String getRandomId(Connection con) throws SQLException {
		String randomId = null;
		int i;
		for (i = 0; i < 10; i++) {
			randomId = Utils.randomId(64);

			synchronized (ID_CREATE_LOCK) {
				try (PreparedStatement preparedStatement = con.prepareStatement("select componentid from componentdata where componentid = ? limit 1;")) {
					preparedStatement.setString(1, randomId);

					if (!preparedStatement.executeQuery().next()) { //if id is unique
						break;
					}
				}
			}
		}

		if (i > 10) {
			LOGGER.warn("");
		}

		return randomId;
	}

	public void delete(Connection con) throws SQLException {
		if (getGroupId() > 0) {
			try (PreparedStatement preparedStatement = con.prepareStatement(
					"delete from componentdata where groupid = ?;"
			)) {
				preparedStatement.setLong(1, getGroupId());

				final int i = preparedStatement.executeUpdate();

				LOGGER.trace("Deleted {} components from group {}", i, groupId);
			}
		} else if (isOneUse()) {
			try (PreparedStatement preparedStatement = con.prepareStatement(
					"delete from componentdata where componentid = ?;"
			)) {
				preparedStatement.setString(1, getComponentId());

				final int i = preparedStatement.executeUpdate();

				if (i > 1) {
					LOGGER.warn("Deleted {} one-use component(s), this should have been only one, component IDs should be unique", i);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "SqlComponentData{" +
				"componentId='" + componentId + '\'' +
				", groupId=" + groupId +
				", oneUse=" + oneUse +
				", ownerId=" + ownerId +
				", expirationTimestamp=" + expirationTimestamp +
				'}';
	}

	public String getComponentId() {
		return componentId;
	}

	public long getGroupId() {
		return groupId;
	}

	public boolean isOneUse() {
		return oneUse;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public long getExpirationTimestamp() {
		return expirationTimestamp;
	}
}
