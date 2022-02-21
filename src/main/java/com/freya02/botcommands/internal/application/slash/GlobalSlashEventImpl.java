package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.LocalizationPath;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.LocalizationManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.interactions.command.SlashCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Locale;

public class GlobalSlashEventImpl extends GlobalSlashEvent {
	private final BContext context;
	private final Method method;

	public GlobalSlashEventImpl(BContext context, Method method, SlashCommandInteractionEvent event) {
		super(event.getJDA(), event.getResponseNumber(), (SlashCommandInteractionImpl) event.getInteraction());
		
		this.context = context;
		this.method = method;
	}

	public BContext getContext() {
		return context;
	}

	@Override
	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		final LocalizationManager localizationManager = ((BContextImpl) context).getLocalizationManager();

		final LocalizationPath localizationBundle = localizationManager.getLocalizationBundle(method);
		final LocalizationPath localizationPrefix = localizationManager.getLocalizationPrefix(method);

		return Localiza;
	}
}
