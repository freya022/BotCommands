package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.localization.Localization;
import com.freya02.botcommands.api.localization.LocalizationPath;
import com.freya02.botcommands.api.localization.LocalizationTemplate;
import com.freya02.botcommands.internal.BContextImpl;
import com.freya02.botcommands.internal.LocalizationManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.interactions.command.SlashCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Locale;

public class GlobalSlashEventImpl extends GlobalSlashEvent {
	private final BContext context;

	public GlobalSlashEventImpl(@NotNull Method method, @NotNull BContext context, @NotNull SlashCommandInteractionEvent event) {
		super(method, event.getJDA(), event.getResponseNumber(), (SlashCommandInteractionImpl) event.getInteraction());
		
		this.context = context;
	}

	public BContext getContext() {
		return context;
	}

	@Override
	@NotNull
	public String localize(@NotNull Locale locale, @NotNull String localizationBundle, @NotNull String localizationPath, @NotNull Localization.Entry @NotNull ... entries) {
		final LocalizationManager localizationManager = ((BContextImpl) context).getLocalizationManager();

		final LocalizationPath localizationPrefix = localizationManager.getLocalizationPrefix(method);

		final Localization instance = Localization.getInstance(localizationBundle, locale);

		if (instance == null) {
			throw new IllegalArgumentException("Found no localization instance for bundle '%s' and locale '%s'".formatted(localizationBundle, locale));
		}

		final String effectivePath = localizationPrefix.resolve(localizationPath).toString();
		final LocalizationTemplate template = instance.get(effectivePath);

		if (template == null) {
			throw new IllegalArgumentException("Found no localization template for '%s' (in bundle '%s' with locale '%s')".formatted(effectivePath, localizationBundle, instance.getEffectiveLocale()));
		}

		return template.localize(entries);
	}
}
