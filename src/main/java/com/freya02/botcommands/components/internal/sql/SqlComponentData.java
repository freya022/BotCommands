package com.freya02.botcommands.components.internal.sql;

import com.freya02.botcommands.Logging;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class SqlComponentData {
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
