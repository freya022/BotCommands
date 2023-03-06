package com.freya02.botcommands.api.components;

import com.google.gson.*;
import gnu.trove.list.array.TLongArrayList;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

/**
 * Controls who can use interactions such as components (button, selection menu).
 * <br>This acts like a while list, if a user or a member has <b>at least one</b> of the requirements, he can use the interaction.
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
	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(TLongArrayList.class, new TLongArrayListAdapter())
			.registerTypeHierarchyAdapter(EnumSet.class, new EnumSetAdapter())
			.create();

	private static class TLongArrayListAdapter implements
			JsonSerializer<TLongArrayList>,
			JsonDeserializer<TLongArrayList> {

		@Override
		public TLongArrayList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			final TLongArrayList list = new TLongArrayList();

			for (JsonElement element : json.getAsJsonArray()) {
				list.add(element.getAsLong());
			}

			return list;
		}

		@Override
		public JsonElement serialize(TLongArrayList src, Type typeOfSrc, JsonSerializationContext context) {
			final JsonArray array = new JsonArray();

			src.forEach(value -> {
				array.add(value);

				return true;
			});

			return array;
		}
	}

	private static class EnumSetAdapter implements
			JsonSerializer<EnumSet<Permission>>,
			JsonDeserializer<EnumSet<Permission>> {
		@Override
		public JsonElement serialize(EnumSet<Permission> src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(Permission.getRaw(src));
		}

		@Override
		public EnumSet<Permission> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return Permission.getPermissions(json.getAsLong());
		}
	}

	private final TLongArrayList userList = new TLongArrayList();
	private final TLongArrayList roleList = new TLongArrayList();
	private final EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);

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
		userList.addAll(userIds);

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
		roleList.addAll(roleIds);

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

	public TLongArrayList getUserList() {
		return userList;
	}

	public TLongArrayList getRoleList() {
		return roleList;
	}

	public EnumSet<Permission> getPermissions() {
		return permissions;
	}

	public boolean isEmpty() {
		return getUserList().isEmpty() && getRoleList().isEmpty() && getPermissions().isEmpty();
	}

	public static InteractionConstraints fromJson(String json) {
		return GSON.fromJson(json, InteractionConstraints.class);
	}

	public String toJson() {
		return GSON.toJson(this);
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
