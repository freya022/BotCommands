package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.ApplicationOptionData;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashUtils;
import com.freya02.botcommands.internal.utils.BResourceBundle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

import static com.freya02.botcommands.internal.application.LocalizedCommandData.LocalizedOption;

public class LocalizationData {
	private final CommandPath localizedPath;
	private final String localizedDescription;
	private final List<LocalizedOption> localizedOptions;
	private final List<List<Command.Choice>> localizedChoices;

	private LocalizationData(@NotNull CommandPath localizedPath,
	                         @Nullable String localizedDescription,
	                         @Nullable List<LocalizedOption> localizedOptions,
	                         @Nullable List<List<Command.Choice>> localizedChoices) {
		this.localizedPath = localizedPath;
		this.localizedDescription = localizedDescription;
		this.localizedOptions = localizedOptions;
		this.localizedChoices = localizedChoices;
	}

	public static LocalizationData getData(BContext context, @Nullable Guild guild, @NotNull ApplicationCommandInfo info) {
		final CommandPath localizedPath;
		final String localizedDescription;
		final List<LocalizedOption> localizedOptions;
		final List<List<Command.Choice>> localizedChoices;

		final Locale locale = context.getEffectiveLocale(guild);

		final BResourceBundle bundle = BResourceBundle.getBundle("BotCommands", locale);

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

		final String qualifier = info.getMethod().getName();

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

			final List<List<Command.Choice>> notLocalizedChoices = SlashUtils.getNotLocalizedChoices(context, guild, info);
			final List<? extends ApplicationCommandParameter<?>> parameters = info.getOptionParameters();
			for (int optionIndex = 0, parametersSize = parameters.size(); optionIndex < parametersSize; optionIndex++) {
				ApplicationCommandParameter<?> parameter = parameters.get(optionIndex);

				final ApplicationOptionData optionData = parameter.getApplicationOptionData();
				final String optionName = tryLocalize(bundle, optionData.getEffectiveName(), prefix, qualifier, "options", optionIndex, "name");
				final String optionDescription = tryLocalize(bundle, optionData.getEffectiveDescription(), prefix, qualifier, "options", optionIndex, "description");

				localizedOptions.add(new LocalizedOption(optionName, optionDescription));

				final List<Command.Choice> choices = getLocalizedChoices(bundle, prefix, qualifier, notLocalizedChoices, optionIndex, parameter);

				localizedChoices.add(choices);
			}
		} else {
			localizedOptions = null;
			localizedChoices = null;
		}

		return new LocalizationData(localizedPath, localizedDescription, localizedOptions, localizedChoices);
	}

	@NotNull
	private static List<Command.Choice> getLocalizedChoices(BResourceBundle bundle,
	                                                             String prefix,
	                                                             String qualifier,
	                                                             List<List<Command.Choice>> notLocalizedChoices,
	                                                             int optionIndex,
	                                                             ApplicationCommandParameter<?> parameter) {
		final List<Command.Choice> choices = new ArrayList<>();

		if (optionIndex < notLocalizedChoices.size()) {
			List<Command.Choice> choiceList = notLocalizedChoices.get(optionIndex);
			for (int i = 0, getSize = choiceList.size(); i < getSize; i++) {
				Command.Choice notLocalizedChoice = choiceList.get(i);

				final String choiceName = tryLocalize(bundle, notLocalizedChoice.getName(), prefix, qualifier, "options", optionIndex, "choices", i, "name");

				//Not really a great idea
				if (parameter.getBoxedType() == Long.class) {
					choices.add(new Command.Choice(choiceName, notLocalizedChoice.getAsLong()));
				} else if (parameter.getBoxedType() == Double.class) {
					choices.add(new Command.Choice(choiceName, notLocalizedChoice.getAsDouble()));
				} else {
					final String choiceValue = tryLocalize(bundle, notLocalizedChoice.getAsString(), prefix, qualifier, "options", optionIndex, "choices", i, "value");

					choices.add(new Command.Choice(choiceName, choiceValue));
				}
			}
		}

		return choices;
	}

	@NotNull
	private static String tryLocalize(@NotNull BResourceBundle bundle, @NotNull String propertyKey, Object... otherPath) {
		final String value = bundle.getPathValue(propertyKey);

		if (value != null) return value;

		return bundle.getPathValueOrDefault(propertyKey, otherPath);
	}

	@NotNull
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
	public List<List<Command.Choice>> getLocalizedChoices() {
		return localizedChoices;
	}
}
