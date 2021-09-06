package com.freya02.botcommands.application;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.BGuildSettings;
import com.freya02.botcommands.SettingsProvider;
import com.freya02.botcommands.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.application.context.user.UserCommandInfo;
import com.freya02.botcommands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.Logging;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Localization {
	private static final Logger LOGGER = Logging.getLogger();

	public static LocalizedCommandData getData(BContext context, @Nullable Guild guild, @Nonnull ApplicationCommandInfo info) {
		final String localizedPath, localizedDescription;
		final List<LocalizedCommandData.LocalizedOption> localizedOptions = new ArrayList<>();
		final List<List<SlashCommand.Choice>> localizedChoices = new ArrayList<>();

		final Locale locale = getLocale(context, guild);

		final ResourceBundle bundle = getBundle(locale);

		if (bundle == null) {
			return null;
		}

		final String prefix;
		if (info instanceof SlashCommandInfo) {
			prefix = "slash";
		} else if (info instanceof MessageCommandInfo) {
			prefix = "message";
		} else if (info instanceof UserCommandInfo) {
			prefix = "user";
		} else {
			throw new IllegalArgumentException("Unsupported application command: " + info.getClass().getName());
		}

		final String dotPath = info.getPath().toString().replace('/', '.');
		localizedPath = getValue(bundle, prefix, dotPath, "path");
		localizedDescription = getValue(bundle, prefix, dotPath, "description");

		int i = 0;
		for (ApplicationCommandParameter<?> parameter : info.getParameters()) {
			if (!parameter.isOption())
				continue;

			String optionName = getValue(bundle, prefix, dotPath, "options", i, "name");
			String optionDescription = getValue(bundle, prefix, dotPath, "options", i, "description");

			if (optionName == null) optionName = parameter.getApplicationOptionData().getEffectiveName();
			if (optionDescription == null) optionDescription = parameter.getApplicationOptionData().getEffectiveDescription();

			localizedOptions.add(new LocalizedCommandData.LocalizedOption(optionName, optionDescription));

			i++;
		}

		return new LocalizedCommandData(localizedPath, localizedDescription, localizedOptions, localizedChoices);
	}

	@Nullable
	private static String getValue(ResourceBundle bundle, Object path, Object... morePath) {
		final StringJoiner joiner = new StringJoiner(".");

		joiner.add(path.toString());

		for (Object s : morePath) {
			joiner.add(s.toString());
		}

		final String key = joiner.toString();

		if (!bundle.containsKey(key)) {
			return null;
		} else {
			return bundle.getString(key);
		}
	}

	private static Locale getLocale(BContext context, @Nullable Guild guild) {
		if (guild != null) {
			final SettingsProvider provider = context.getSettingsProvider();

			if (provider != null) {
				final BGuildSettings settings = provider.getSettings(guild.getIdLong());

				if (settings != null) {
					return settings.getLocale();
				}
			}
		}

		return Locale.getDefault();
	}

	@Nullable
	private static ResourceBundle getBundle(Locale locale) {
		try {
			final ResourceBundle bundle = ResourceBundle.getBundle("BotCommands", locale);
			if (bundle.getLocale().toString().isBlank() && !locale.toString().isBlank()) { //Check if the bundle loaded is not a fallback
				if (Locale.getDefault() != locale) { //No need to warn when the bundle choosed is the default one when the locale asked is the default one
					LOGGER.warn("Tried to use a BotCommands bundle with locale '{}' but none was found, using default bundle", locale);
				}
			}

			return bundle;
		} catch (MissingResourceException e) {
			LOGGER.warn("Can't find resource BotCommands.properties which is used for localized strings, localization won't be used");

			return null;
		}
	}
}
