package com.freya02.botcommands.internal;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.annotations.CommandId;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.runner.MethodRunner;
import com.freya02.botcommands.internal.utils.AnnotationUtils;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.function.Function;

import static com.freya02.botcommands.internal.utils.AnnotationUtils.*;

/**
 * @param <T> Command instance type
 */
public abstract class AbstractCommandInfo<T> extends Cooldownable implements ExecutableInteractionInfo {
	private final T instance;

	/** This is NOT localized */
	protected final CommandPath path;
	protected final boolean ownerRequired;
	protected final Method commandMethod;

	protected final EnumSet<Permission> userPermissions;
	protected final EnumSet<Permission> botPermissions;

	private final String commandId;
	private final MethodRunner methodRunner;

	@SafeVarargs
	protected <A extends Annotation> AbstractCommandInfo(@NotNull BContext context,
	                                                     @NotNull T instance,
	                                                     @NotNull A annotation,
	                                                     @NotNull Method commandMethod,
	                                                     Function<A, String>... nameComponentsFunctions) {
		super(getEffectiveCooldownStrategy(commandMethod));

		this.instance = instance;

		final String[] pathComponents = new String[nameComponentsFunctions.length];
		for (int i = 0; i < nameComponentsFunctions.length; i++) {
			final String component = nameComponentsFunctions[i].apply(annotation);

			if (component.isEmpty()) {
				pathComponents[i] = null; //We need to transform blank strings to null to conform with CommandPath
			} else {
				pathComponents[i] = component;
			}
		}

		this.path = CommandPath.of(pathComponents);
		this.commandMethod = commandMethod;
		this.methodRunner = context.getMethodRunnerFactory().make(instance, commandMethod);

		this.ownerRequired = AnnotationUtils.getEffectiveRequireOwnerState(commandMethod);

		final CommandId commandIdAnnot = commandMethod.getAnnotation(CommandId.class);
		this.commandId = commandIdAnnot != null
				? commandIdAnnot.value()
				: null;

		this.userPermissions = getEffectiveUserPermissions(commandMethod);
		this.botPermissions = getEffectiveBotPermissions(commandMethod);
	}

	@Override
	@NotNull
	public T getInstance() {
		return instance;
	}

	@Override
	@NotNull
	public Method getMethod() {
		return commandMethod;
	}

	@Override
	@NotNull
	public MethodRunner getMethodRunner() {
		return methodRunner;
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

	public boolean isOwnerRequired() {
		return ownerRequired;
	}

	@Nullable
	public String getCommandId() {
		return commandId;
	}
}
