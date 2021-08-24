package com.freya02.botcommands.application;

import com.freya02.botcommands.Cooldownable;
import com.freya02.botcommands.annotation.RequireOwner;
import com.freya02.botcommands.application.slash.ApplicationCommand;
import net.dv8tion.jda.api.Permission;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;

import static com.freya02.botcommands.internal.utils.AnnotationUtils.getAnnotationValue;

public abstract class ApplicationCommandInfo extends Cooldownable {
	private final ApplicationCommand instance;
	/** This is NOT localized */
	protected final String name;
	protected final boolean guildOnly, ownerOnly;
	protected final Method commandMethod;

	protected final EnumSet<Permission> userPermissions = EnumSet.noneOf(Permission.class);
	protected final EnumSet<Permission> botPermissions = EnumSet.noneOf(Permission.class);

	protected <A extends Annotation> ApplicationCommandInfo(@Nonnull ApplicationCommand instance, @Nonnull A annotation, @Nonnull String name, @Nonnull Method commandMethod) {
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

	@Nonnull
	public ApplicationCommand getInstance() {
		return instance;
	}

	/** This is NOT localized */
	public String getName() {
		return name;
	}

	public abstract String getBaseName();

	/** This is NOT localized */
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
