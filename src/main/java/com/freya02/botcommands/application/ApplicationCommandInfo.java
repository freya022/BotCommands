package com.freya02.botcommands.application;

import com.freya02.botcommands.Cooldownable;
import com.freya02.botcommands.annotation.RequireOwner;
import net.dv8tion.jda.api.Permission;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;

import static com.freya02.botcommands.internal.utils.AnnotationUtils.getAnnotationValue;

public abstract class ApplicationCommandInfo extends Cooldownable {
	private final Object instance;
	/** This is NOT localized */
	protected final String name;
	protected final boolean guildOnly, ownerOnly;
	protected final Method commandMethod;

	protected final EnumSet<Permission> userPermissions = EnumSet.noneOf(Permission.class);
	protected final EnumSet<Permission> botPermissions = EnumSet.noneOf(Permission.class);

	protected <A extends Annotation> ApplicationCommandInfo(Object instance, A annotation, String name, Method commandMethod) {
		super(getAnnotationValue(annotation, "cooldownScope"),
				getAnnotationValue(annotation, "cooldown"));
		this.instance = instance;

		this.name = name;
		this.guildOnly = getAnnotationValue(annotation, "guildOnly");
		this.ownerOnly = commandMethod.isAnnotationPresent(RequireOwner.class);
		this.commandMethod = commandMethod;

		Permission[] userPermissions = getAnnotationValue(annotation, "userPermissions");
		Permission[] botPermissions = getAnnotationValue(annotation, "botPermissions");

		if ((userPermissions.length != 0 || botPermissions.length != 0) && !guildOnly)
			throw new IllegalArgumentException(commandMethod + " : application command with permissions should be guild-only");

		Collections.addAll(this.userPermissions, userPermissions);
		Collections.addAll(this.botPermissions, botPermissions);
	}

	public Object getInstance() {
		return instance;
	}

	/** This is NOT localized */
	public String getName() {
		return name;
	}

	public abstract String getPath();

	public abstract int getPathComponents();

	public boolean isGuildOnly() {
		return guildOnly;
	}

	public EnumSet<Permission> getUserPermissions() {
		return userPermissions;
	}

	public EnumSet<Permission> getBotPermissions() {
		return botPermissions;
	}

	public Method getCommandMethod() {
		return commandMethod;
	}

	public boolean isOwnerOnly() {
		return ownerOnly;
	}
}
