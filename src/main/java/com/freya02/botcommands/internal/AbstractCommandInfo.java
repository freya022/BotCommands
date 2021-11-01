package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.application.CommandParameter;
import com.freya02.botcommands.internal.utils.AnnotationUtils;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.freya02.botcommands.internal.utils.AnnotationUtils.*;

/**
 * @param <T> Command instance type
 */
public abstract class AbstractCommandInfo<T> extends Cooldownable {
	private final T instance;

	/** This is NOT localized */
	protected final CommandPath path;
	protected final boolean ownerRequired;
	protected final Method commandMethod;

	protected final EnumSet<Permission> userPermissions;
	protected final EnumSet<Permission> botPermissions;

	private final NSFWState nsfwState;

	protected <A extends Annotation> AbstractCommandInfo(@NotNull T instance,
	                                                     @NotNull A annotation,
	                                                     @NotNull Method commandMethod,
	                                                     String... nameComponents) {
		super(getEffectiveCooldownStrategy(commandMethod));

		this.instance = instance;

		for (int i = 0; i < nameComponents.length; i++) {
			if (nameComponents[i].isBlank()) {
				nameComponents[i] = null; //We need to transform blank strings to null to conform with CommandPath
			}
		}

		this.path = CommandPath.of(nameComponents);
		this.commandMethod = commandMethod;

		this.ownerRequired = AnnotationUtils.getEffectiveRequireOwnerState(commandMethod);
		this.nsfwState = NSFWState.ofMethod(commandMethod);

		this.userPermissions = getEffectiveUserPermissions(commandMethod);
		this.botPermissions = getEffectiveBotPermissions(commandMethod);
	}

	@NotNull
	public T getInstance() {
		return instance;
	}

	/** This is NOT localized */
	public CommandPath getPath() {
		return path;
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

	public boolean isOwnerRequired() {
		return ownerRequired;
	}

	public abstract MethodParameters<? extends CommandParameter<?>> getParameters();

	public List<? extends CommandParameter<?>> getOptionParameters() {
		return getParameters().stream().filter(CommandParameter::isOption).collect(Collectors.toList());
	}

	@Nullable
	public NSFWState getNSFWState() {
		return nsfwState;
	}
}
