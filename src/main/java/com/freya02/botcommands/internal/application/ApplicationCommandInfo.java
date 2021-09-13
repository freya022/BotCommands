package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.annotations.RequireOwner;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.Cooldownable;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.freya02.botcommands.internal.utils.AnnotationUtils.getAnnotationValue;

public abstract class ApplicationCommandInfo extends Cooldownable {
	private final ApplicationCommand instance;
	/** This is NOT localized */
	protected final CommandPath path;
	protected final boolean guildOnly, ownerOnly;
	protected final Method commandMethod;

	protected final EnumSet<Permission> userPermissions = EnumSet.noneOf(Permission.class);
	protected final EnumSet<Permission> botPermissions = EnumSet.noneOf(Permission.class);

	protected <A extends Annotation> ApplicationCommandInfo(@NotNull ApplicationCommand instance, @NotNull A annotation, @NotNull Method commandMethod, String... nameComponents) {
		super(getAnnotationValue(annotation, "cooldownScope"),
				getAnnotationValue(annotation, "cooldown"));
		this.instance = instance;

		for (int i = 0; i < nameComponents.length; i++) {
			if (nameComponents[i].isBlank()) {
				nameComponents[i] = null; //We need to transform blank strings to null to conform with CommandPath
			}
		}

		this.path = CommandPath.of(nameComponents);
		this.guildOnly = getAnnotationValue(annotation, "guildOnly");
		this.ownerOnly = commandMethod.isAnnotationPresent(RequireOwner.class);
		this.commandMethod = commandMethod;

		Permission[] userPermissions = getAnnotationValue(annotation, "userPermissions");
		Permission[] botPermissions = getAnnotationValue(annotation, "botPermissions");

		if ((userPermissions.length != 0 || botPermissions.length != 0) && !guildOnly)
			throw new IllegalArgumentException(Utils.formatMethodShort(commandMethod) + " : application command with permissions should be guild-only");

		Collections.addAll(this.userPermissions, userPermissions);
		Collections.addAll(this.botPermissions, botPermissions);
	}

	@NotNull
	public ApplicationCommand getInstance() {
		return instance;
	}

	/** This is NOT localized */
	public CommandPath getPath() {
		return path;
	}

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
	
	public abstract MethodParameters<? extends ApplicationCommandParameter<?>> getParameters();

	public List<? extends ApplicationCommandParameter<?>> getOptionParameters() {
		return getParameters().stream().filter(ApplicationCommandParameter::isOption).collect(Collectors.toList());
	}
}
