package com.freya02.botcommands.api.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Controls who can use interactions such as components (button, selection menu)
 * <br>This acts like a while list, if an user or a member has one of the requirements, he can use the interaction
 * <br>You can filter by:
 * <ul>
 *     <li>User ID</li>
 *     <li>Role ID</li>
 *     <li>Permissions</li>
 * </ul>
 *
 * <b>See the static methods</b>
 */
public class InteractionConstraints {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		//Lmao jackson inserts an invisible "empty" property so it fails to deserialize
		MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	private final List<Long> userList = new ArrayList<>();
	private final List<Long> roleList = new ArrayList<>();
	private final List<Permission> permissions = new ArrayList<>();

	public static InteractionConstraints empty() {
		return new InteractionConstraints();
	}

	public static InteractionConstraints ofUserIds(long... userIds) {
		return empty().addUserIds(userIds);
	}

	public static InteractionConstraints ofUserIds(Collection<Long> userIds) {
		return empty().addUserIds(userIds);
	}

	public static InteractionConstraints ofUsers(User... users) {
		return empty().addUsers(users);
	}

	public static InteractionConstraints ofUsers(Collection<User> users) {
		return empty().addUsers(users);
	}

	public static InteractionConstraints ofRoleIds(long @NotNull ... roleIds) {
		return empty().addRoleIds(roleIds);
	}

	public static InteractionConstraints ofRoleIds(@NotNull Collection<@NotNull Long> roleIds) {
		return empty().addRoleIds(roleIds);
	}

	public static InteractionConstraints ofRoles(@NotNull Role @NotNull ... roles) {
		return empty().addRoles(roles);
	}

	public static InteractionConstraints ofRoles(@NotNull Collection<@NotNull Role> roles) {
		return empty().addRoles(roles);
	}

	public static InteractionConstraints ofPermissions(Permission @NotNull ... permissions) {
		return empty().addPermissions(permissions);
	}

	public InteractionConstraints addUserIds(long @NotNull ... userIds) {
		for (long userId : userIds) {
			userList.add(userId);
		}

		return this;
	}

	public InteractionConstraints addUserIds(Collection<@NotNull Long> userIds) {
		userList.addAll(userIds);

		return this;
	}

	public InteractionConstraints addUsers(@NotNull User @NotNull ... userIds) {
		for (User user : userIds) {
			userList.add(user.getIdLong());
		}

		return this;
	}

	public InteractionConstraints addUsers(@NotNull Collection<@NotNull User> users) {
		for (User user : users) {
			userList.add(user.getIdLong());
		}

		return this;
	}

	public InteractionConstraints addRoleIds(long @NotNull ... roleIds) {
		for (long roleId : roleIds) {
			roleList.add(roleId);
		}

		return this;
	}

	public InteractionConstraints addRoleIds(Collection<Long> roleIds) {
		roleList.addAll(roleIds);

		return this;
	}

	public InteractionConstraints addRoles(@NotNull Role @NotNull ... roles) {
		for (Role role : roles) {
			roleList.add(role.getIdLong());
		}

		return this;
	}

	public InteractionConstraints addRoles(@NotNull Collection<@NotNull Role> roles) {
		for (Role role : roles) {
			roleList.add(role.getIdLong());
		}

		return this;
	}

	public InteractionConstraints addPermissions(Permission... permissions) {
		Collections.addAll(this.permissions, permissions);

		return this;
	}

	public List<Long> getUserList() {
		return userList;
	}

	public List<Long> getRoleList() {
		return roleList;
	}

	public List<Permission> getPermissions() {
		return permissions;
	}

	public boolean isEmpty() {
		return getUserList().isEmpty() && getRoleList().isEmpty() && getPermissions().isEmpty();
	}

	public static InteractionConstraints fromJson(String json) {
		try {
			return MAPPER.readValue(json, InteractionConstraints.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Unable to write JSON of ComponentConstraints", e);
		}
	}

	public String toJson() {
		try {
			return MAPPER.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Unable to write JSON of ComponentConstraints", e);
		}
	}

	public InteractionConstraints setConstraints(InteractionConstraints otherConstraints) {
		getUserList().clear();
		getRoleList().clear();
		getPermissions().clear();

		getUserList().addAll(otherConstraints.getUserList());
		getRoleList().addAll(otherConstraints.getRoleList());
		getPermissions().addAll(otherConstraints.getPermissions());

		return this;
	}
}
