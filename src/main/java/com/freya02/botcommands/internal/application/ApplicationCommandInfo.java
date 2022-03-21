package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
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
	protected final boolean guildOnly;
	protected final boolean testOnly;

	@SafeVarargs
	protected <A extends Annotation> ApplicationCommandInfo(@NotNull BContext context,
	                                                        @NotNull ApplicationCommand instance,
	                                                        @NotNull A annotation,
	                                                        @NotNull Method commandMethod,
	                                                        Function<A, String>... nameComponentsFunctions) {
		super(context, instance, annotation, commandMethod, nameComponentsFunctions);

		this.guildOnly = context.getApplicationCommandsContext().isForceGuildCommandsEnabled()
				|| (boolean) AnnotationUtils.getAnnotationValue(annotation, "guildOnly");
		this.testOnly = AnnotationUtils.getEffectiveTestState(commandMethod);

		if (testOnly && !isGuildOnly()) {
			throw new IllegalArgumentException(Utils.formatMethodShort(commandMethod) + " : application command annotated with @Test must be a guild-only command");
		}

		if (isOwnerRequired() && !isGuildOnly()) {
			//TODO remove when i can finally work out privileges for global commands in a guild context
			// When discord adds native localisation
			throw new IllegalArgumentException(Utils.formatMethodShort(commandMethod) + " : global application commands cannot have privileges");
		}

		if ((userPermissions.size() != 0 || botPermissions.size() != 0) && !guildOnly)
			throw new IllegalArgumentException(Utils.formatMethodShort(commandMethod) + " : application command with permissions should be guild-only");

		if (getCommandId() != null && !isGuildOnly()) {
			throw new IllegalArgumentException(Utils.formatMethodShort(commandMethod) + " : application command with guild-specific ID should be guild-only");
		}
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
