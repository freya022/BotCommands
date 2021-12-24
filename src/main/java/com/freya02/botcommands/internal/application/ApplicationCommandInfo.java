package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.internal.AbstractCommandInfo;
import com.freya02.botcommands.internal.MethodParameters;
import com.freya02.botcommands.internal.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import static com.freya02.botcommands.internal.utils.AnnotationUtils.getAnnotationValue;

public abstract class ApplicationCommandInfo extends AbstractCommandInfo<ApplicationCommand> {
	protected final boolean guildOnly;

	protected <A extends Annotation> ApplicationCommandInfo(@NotNull BContext context, @NotNull ApplicationCommand instance, @NotNull A annotation, @NotNull Method commandMethod, String... nameComponents) {
		super(context, instance, commandMethod, nameComponents);

		this.guildOnly = getAnnotationValue(annotation, "guildOnly");

		if ((userPermissions.size() != 0 || botPermissions.size() != 0) && !guildOnly)
			throw new IllegalArgumentException(Utils.formatMethodShort(commandMethod) + " : application command with permissions should be guild-only");
	}

	public boolean isGuildOnly() {
		return guildOnly;
	}

	public abstract MethodParameters<? extends ApplicationCommandParameter<?>> getParameters();

	@SuppressWarnings("unchecked")
	@Override
	public List<? extends ApplicationCommandParameter<?>> getOptionParameters() {
		return (List<? extends ApplicationCommandParameter<?>>) super.getOptionParameters();
	}

	public LocalizedCommandData getLocalizedData(@NotNull BContext context, @Nullable Guild guild) {
		return LocalizedCommandData.of(context, guild, this);
	}
}
