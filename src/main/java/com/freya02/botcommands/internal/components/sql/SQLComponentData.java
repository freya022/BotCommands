package com.freya02.botcommands.internal.components.sql;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.components.InteractionConstraints;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class SQLComponentData {
	private static final Logger LOGGER = Logging.getLogger();

	private final String componentId;
	private final long groupId;
	private final boolean oneUse;
	private final InteractionConstraints interactionConstraints;
	private final long expirationTimestamp;

	public SQLComponentData(String componentId, long groupId, boolean oneUse, InteractionConstraints interactionConstraints, long expirationTimestamp) {
		this.componentId = componentId;
		this.groupId = groupId;
		this.oneUse = oneUse;
		this.interactionConstraints = interactionConstraints;
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
		} else {
			try (PreparedStatement preparedStatement = con.prepareStatement(
					"delete from componentdata where componentid = ?;"
			)) {
				preparedStatement.setString(1, getComponentId());

				final int i = preparedStatement.executeUpdate();

				if (i > 1) {
					LOGGER.warn("Deleted {} one-use component(s), this should have been only one, component IDs should be unique", i);
				}

				LOGGER.trace("Deleted component {}", getComponentId());
			}
		}
	}

	@Override
	public String toString() {
		return "SqlComponentData{" +
				"componentId='" + componentId + '\'' +
				", groupId=" + groupId +
				", oneUse=" + oneUse +
				", componentConstraints=" + interactionConstraints +
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

	public InteractionConstraints getInteractionConstraints() {
		return interactionConstraints;
	}

	public long getExpirationTimestamp() {
		return expirationTimestamp;
	}
}
