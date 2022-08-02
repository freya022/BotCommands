package com.freya02.botcommands.internal.components.sql;

import com.freya02.botcommands.api.Logging;
import com.freya02.botcommands.api.components.ComponentErrorReason;
import com.freya02.botcommands.api.components.InteractionConstraints;
import com.freya02.botcommands.internal.components.HandleComponentResult;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
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
					"delete from bc_component_data where group_id = ?;"
			)) {
				preparedStatement.setLong(1, getGroupId());

				final int i = preparedStatement.executeUpdate();

				LOGGER.trace("Deleted {} components from group {}", i, groupId);
			}
		} else {
			try (PreparedStatement preparedStatement = con.prepareStatement(
					"delete from bc_component_data where component_id = ?;"
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
				"component_id='" + componentId + '\'' +
				", group_id=" + groupId +
				", one_use=" + oneUse +
				", componentConstraints=" + interactionConstraints +
				", expiration_timestamp=" + expirationTimestamp +
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

	@NotNull
	public HandleComponentResult handleComponentData(GenericComponentInteractionCreateEvent event) {
		final boolean oneUse = this.isOneUse() || this.getGroupId() > 0;
		final InteractionConstraints constraints = this.getInteractionConstraints();
		final long expirationTimestamp = this.getExpirationTimestamp();

		if (expirationTimestamp > 0 && System.currentTimeMillis() > expirationTimestamp) {
			return new HandleComponentResult(ComponentErrorReason.EXPIRED, true);
		}

		boolean allowed = checkConstraints(event, constraints);

		if (!allowed) {
			return new HandleComponentResult(ComponentErrorReason.NOT_ALLOWED, false);
		}

		return new HandleComponentResult(null, oneUse);
	}

	private boolean checkConstraints(GenericComponentInteractionCreateEvent event, InteractionConstraints constraints) {
		if (constraints.isEmpty()) return true;

		if (constraints.getUserList().contains(event.getUser().getIdLong())) {
			return true;
		}

		final Member member = event.getMember();
		if (member != null) {
			if (!constraints.getPermissions().isEmpty()) {
				if (member.hasPermission(event.getGuildChannel(), constraints.getPermissions())) {
					return true;
				}
			}

			for (Role role : member.getRoles()) {
				boolean hasRole = constraints.getRoleList().contains(role.getIdLong());

				if (hasRole) {
					return true;
				}
			}
		}

		return false;
	}
}
