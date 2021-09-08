package com.freya02.botcommands.application;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.BGuildSettings;
import com.freya02.botcommands.SettingsProvider;
import com.freya02.botcommands.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.application.context.user.UserCommandInfo;
import com.freya02.botcommands.application.slash.SlashCommandInfo;
import com.freya02.botcommands.application.slash.SlashUtils;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.Logging;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.freya02.botcommands.application.LocalizedCommandData.LocalizedOption;

class LocalizationData {
	private static final Logger LOGGER = Logging.getLogger();
	private static final Set<Locale> warnedMissingLocales = new HashSet<>();
	private static boolean warnedMissingBundle;

	private final CommandPath localizedPath;
	private final String localizedDescription;
	private final List<LocalizedOption> localizedOptions;
	private final List<List<SlashCommand.Choice>> localizedChoices;

	private LocalizationData(@Nonnull CommandPath localizedPath,
	                         @Nullable String localizedDescription,
	                         @Nullable List<LocalizedOption> localizedOptions,
	                         @Nullable List<List<SlashCommand.Choice>> localizedChoices) {
		this.localizedPath = localizedPath;
		this.localizedDescription = localizedDescription;
		this.localizedOptions = localizedOptions;
		this.localizedChoices = localizedChoices;
	}

	public static LocalizationData getData(BContext context, @Nullable Guild guild, @Nonnull ApplicationCommandInfo info) {
		final CommandPath localizedPath;
		final String localizedDescription;
		final List<LocalizedOption> localizedOptions;
		final List<List<SlashCommand.Choice>> localizedChoices;

		final Locale locale = getLocale(context, guild);

		final ResourceBundle bundle = getBundle(locale);

		if (bundle == null) {
			return null;
		}

		final String prefix;
		if (info instanceof SlashCommandInfo) {
			prefix = "slash";
		} else if (info instanceof UserCommandInfo) {
			prefix = "user";
		} else if (info instanceof MessageCommandInfo) {
			prefix = "message";
		} else {
			throw new IllegalArgumentException("Unknown localization prefix for class: " + info.getClass().getSimpleName());
		}

		final String qualifier = info.getCommandMethod().getName();

		final StringJoiner pathJoiner = new StringJoiner("/");
		pathJoiner.add(tryLocalize(bundle, info.getPath().getName(), prefix, qualifier, "name"));

		if (info instanceof SlashCommandInfo) {
			final String notLocalizedGroup = info.getPath().getGroup();
			final String notLocalizedSubname = info.getPath().getSubname();

			if (notLocalizedGroup != null) pathJoiner.add(tryLocalize(bundle, notLocalizedGroup, prefix, qualifier, "group"));
			if (notLocalizedSubname != null) pathJoiner.add(tryLocalize(bundle, notLocalizedSubname, prefix, qualifier, "subname"));
		}

		localizedPath = CommandPath.of(pathJoiner.toString());

		if (info instanceof SlashCommandInfo) {
			localizedDescription = tryLocalize(bundle, ((SlashCommandInfo) info).getDescription(), prefix, qualifier, "description");
		} else localizedDescription = null;

		if (info instanceof SlashCommandInfo) {
			localizedOptions = new ArrayList<>();
			localizedChoices = new ArrayList<>();

			final List<List<SlashCommand.Choice>> notLocalizedChoices = SlashUtils.getNotLocalizedChoices(context, guild, info);
			final List<? extends ApplicationCommandParameter<?>> parameters = info.getOptionParameters();
			for (int optionIndex = 0, parametersSize = parameters.size(); optionIndex < parametersSize; optionIndex++) {
				ApplicationCommandParameter<?> parameter = parameters.get(optionIndex);

				final ApplicationOptionData optionData = parameter.getApplicationOptionData();
				final String optionName = tryLocalize(bundle, optionData.getEffectiveName(), prefix, qualifier, "options", optionIndex, "name");
				final String optionDescription = tryLocalize(bundle, optionData.getEffectiveDescription(), prefix, qualifier, "options", optionIndex, "description");

				localizedOptions.add(new LocalizedOption(optionName, optionDescription));

				final List<SlashCommand.Choice> choices = getLocalizedChoices(bundle, prefix, qualifier, notLocalizedChoices, optionIndex, parameter);

				localizedChoices.add(choices);
			}
		} else {
			localizedOptions = null;
			localizedChoices = null;
		}

		return new LocalizationData(localizedPath, localizedDescription, localizedOptions, localizedChoices);
	}

	@Nonnull
	private static List<SlashCommand.Choice> getLocalizedChoices(ResourceBundle bundle,
	                                                             String prefix,
	                                                             String qualifier,
	                                                             List<List<SlashCommand.Choice>> notLocalizedChoices,
	                                                             int optionIndex,
	                                                             ApplicationCommandParameter<?> parameter) {
		final List<SlashCommand.Choice> choices = new ArrayList<>();

		if (optionIndex < notLocalizedChoices.size()) {
			List<SlashCommand.Choice> choiceList = notLocalizedChoices.get(optionIndex);
			for (int i = 0, getSize = choiceList.size(); i < getSize; i++) {
				SlashCommand.Choice notLocalizedChoice = choiceList.get(i);

				final String choiceName = tryLocalize(bundle, notLocalizedChoice.getName(), prefix, qualifier, "options", optionIndex, "choices", i, "name");

				//Not really a great idea
				if (parameter.getType() == long.class || parameter.getType() == Long.class) {
					choices.add(new SlashCommand.Choice(choiceName, notLocalizedChoice.getAsLong()));
				} else if (parameter.getType() == double.class || parameter.getType() == Double.class) {
					choices.add(new SlashCommand.Choice(choiceName, notLocalizedChoice.getAsDouble()));
				} else {
					final String choiceValue = tryLocalize(bundle, notLocalizedChoice.getAsString(), prefix, qualifier, "options", optionIndex, "choices", i, "value");

					choices.add(new SlashCommand.Choice(choiceName, choiceValue));
				}
			}
		}

		return choices;
	}

	@NotNull
	private static String tryLocalize(@NotNull ResourceBundle bundle, @NotNull String propertyKey, Object... otherPath) {
		final String value = getValue(bundle, propertyKey);

		if (value != null) return value;

		final String value2 = getValue(bundle, otherPath);

		return Objects.requireNonNullElse(value2, propertyKey);
	}

	@Nullable
	private static String getValue(ResourceBundle bundle, Object... morePath) {
		final StringJoiner joiner = new StringJoiner(".");

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
					if (warnedMissingLocales.add(locale)) {
						LOGGER.warn("Tried to use a BotCommands bundle with locale '{}' but none was found, using default bundle", locale);
					}
				}
			}

			return bundle;
		} catch (MissingResourceException e) {
			if (!warnedMissingBundle) {
				warnedMissingBundle = true;

				LOGGER.warn("Can't find resource BotCommands.properties which is used for localized strings, localization won't be used");
			}

			return null;
		}
	}

	@Nonnull
	public CommandPath getLocalizedPath() {
		return localizedPath;
	}

	@Nullable
	public String getLocalizedDescription() {
		return localizedDescription;
	}

	@Nullable
	public List<LocalizedOption> getLocalizedOptions() {
		return localizedOptions;
	}

	@Nullable
	public List<List<SlashCommand.Choice>> getLocalizedChoices() {
		return localizedChoices;
	}
}
