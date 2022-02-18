package com.freya02.botcommands.api.components.builder;

import com.freya02.botcommands.api.components.InteractionConstraints;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@SuppressWarnings("unchecked")
public interface ComponentBuilder<T extends ComponentBuilder<T>> {
	/**
	 * Makes this component usable only once<br>
	 * This means once it is clicked if all the checks are valid and thus the handler has been executed, this won't be usable anymore
	 *
	 * @return This component builder for chaining purposes
	 */
	T oneUse();

	boolean isOneUse();

	InteractionConstraints getInteractionConstraints();

	default T setConstraints(@NotNull InteractionConstraints constraints) {
		getInteractionConstraints().setConstraints(constraints);

		return (T) this;
	}

	default T addUserIds(long... userIds) {
		getInteractionConstraints().addUserIds(userIds);

		return (T) this;
	}

	default T addUserIds(Collection<@NotNull Long> userIds) {
		getInteractionConstraints().addUserIds(userIds);

		return (T) this;
	}

	default T addUsers(@NotNull User @NotNull ... userIds) {
		getInteractionConstraints().addUsers(userIds);

		return (T) this;
	}

	default T addUsers(@NotNull Collection<@NotNull User> users) {
		getInteractionConstraints().addUsers(users);

		return (T) this;
	}

	default T addRoleIds(long... roleIds) {
		getInteractionConstraints().addRoleIds(roleIds);

		return (T) this;
	}

	default T addRoleIds(Collection<Long> roleIds) {
		getInteractionConstraints().addRoleIds(roleIds);

		return (T) this;
	}

	default T addRoles(@NotNull Role @NotNull ... roles) {
		getInteractionConstraints().addRoles(roles);

		return (T) this;
	}

	default T addRoles(@NotNull Collection<@NotNull Role> roles) {
		getInteractionConstraints().addRoles(roles);

		return (T) this;
	}

	default T addPermissions(Permission... permissions) {
		getInteractionConstraints().addPermissions(permissions);

		return (T) this;
	}
}
