package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.internal.AbstractCommandInfo;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.utils.AnnotationUtils;
import com.freya02.botcommands.internal.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

public abstract class ApplicationCommandInfo extends AbstractCommandInfo<ApplicationCommand> {
	protected final CommandScope scope;
	protected final boolean defaultLocked;
	protected final boolean guildOnly;
	protected final boolean testOnly;

	@SafeVarargs
	protected <A extends Annotation> ApplicationCommandInfo(@NotNull BContext context,
	                                                        @NotNull ApplicationCommand instance,
	                                                        @NotNull A annotation,
	                                                        @NotNull Method commandMethod,
	                                                        Function<A, String>... nameComponentsFunctions) {
		super(context, instance, annotation, commandMethod, nameComponentsFunctions);

		this.scope = AnnotationUtils.getAnnotationValue(annotation, "scope");
		this.defaultLocked = AnnotationUtils.getAnnotationValue(annotation, "defaultLocked");
		this.guildOnly = context.getApplicationCommandsContext().isForceGuildCommandsEnabled() || scope.isGuildOnly();
		this.testOnly = AnnotationUtils.getEffectiveTestState(commandMethod);

		if (testOnly && scope != CommandScope.GUILD) {
			throw new IllegalArgumentException(Utils.formatMethodShort(commandMethod) + " : application command annotated with @Test must have the GUILD scope");
		}

		if (isOwnerRequired()) {
			throw new IllegalArgumentException(Utils.formatMethodShort(commandMethod) + " : application commands cannot be marked as owner-only");
		}

		//Administrators manage who can use what, bot doesn't need to check for user mistakes
		// Why would you ask for a permission if the administrators want a less-powerful user to be able to use it ?
		if (isDefaultLocked()) {
			userPermissions.clear();
		}

		if ((userPermissions.size() != 0 || botPermissions.size() != 0) && !guildOnly)
			throw new IllegalArgumentException(Utils.formatMethodShort(commandMethod) + " : application command with permissions should be guild-only");

		if (getCommandId() != null && scope != CommandScope.GUILD) {
			throw new IllegalArgumentException(Utils.formatMethodShort(commandMethod) + " : application command with guild-specific ID must have the GUILD scope");
		}
	}

	public CommandScope getScope() {
		return scope;
	}

	public boolean isDefaultLocked() {
		return defaultLocked;
	}

	public boolean isGuildOnly() {
		return guildOnly;
	}

	public boolean isTestOnly() {
		return testOnly;
	}

	@NotNull
	public abstract MethodParameters<? extends ApplicationCommandParameter<?>> getParameters();

	@SuppressWarnings("unchecked")
	@Override
	@NotNull
	public List<? extends ApplicationCommandParameter<?>> getOptionParameters() {
		return (List<? extends ApplicationCommandParameter<?>>) super.getOptionParameters();
	}
}
