package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import com.freya02.botcommands.internal.application.slash.SlashUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LocalizedCommandData {
	@NotNull private final CommandPath localizedPath;
	@NotNull private final String localizedDescription;
	@NotNull private final List<LocalizedOption> localizedOptionNames;
	@NotNull private final List<List<SlashCommand.Choice>> localizedOptionChoices;

	/**
	 * Constructs new localized application command data for a specific Guild
	 *
	 * @param localizedPath          The path of the command, this should look like this <code>name/subgroup/subcommand</code>
	 * @param localizedDescription   The description of the command, <i>not used for context commands</i>
	 * @param localizedOptionNames   The list of option names (parameters) of the command, <i>not used for context commands</i>
	 * @param localizedOptionChoices The list of choices for all the command options, each entry of this list is the choices for one parameter, <i>not used for context commands</i>
	 *                               <br> <i>Choice list n°0 is the choice list for option n°0 and such</i>
	 */
	private LocalizedCommandData(@NotNull CommandPath localizedPath,
	                             @NotNull String localizedDescription,
	                             @NotNull List<LocalizedOption> localizedOptionNames,
	                             @NotNull List<List<SlashCommand.Choice>> localizedOptionChoices) {
		this.localizedPath = localizedPath;
		this.localizedDescription = localizedDescription;
		this.localizedOptionNames = localizedOptionNames;
		this.localizedOptionChoices = localizedOptionChoices;
	}

	public static LocalizedCommandData of(@NotNull BContext context, @Nullable Guild guild, @NotNull ApplicationCommandInfo info) {
		final LocalizationData data = LocalizationData.getData(context, guild, info);

		final CommandPath localizedPath;
		if (data != null) {
			localizedPath = data.getLocalizedPath();
		} else {
			localizedPath = info.getPath();
		}

		if (!(info instanceof SlashCommandInfo))
			return new LocalizedCommandData(localizedPath, "No description", Collections.emptyList(), Collections.emptyList());

		final String localizedDescription;
		final List<LocalizedOption> localizedOptions;
		final List<List<SlashCommand.Choice>> localizedChoices;

		if (data != null && data.getLocalizedDescription() != null) {
			localizedDescription = data.getLocalizedDescription();
		} else {
			localizedDescription = ((SlashCommandInfo) info).getDescription();
		}

		if (data != null && data.getLocalizedOptions() != null) {
			localizedOptions = data.getLocalizedOptions();
		} else {
			localizedOptions = info.getOptionParameters()
					.stream()
					.filter(ApplicationCommandParameter::isOption)
					.map(ApplicationCommandParameter::getApplicationOptionData)
					.map(a -> new LocalizedOption(a.getEffectiveName(), a.getEffectiveDescription()))
					.collect(Collectors.toList());
		}

		if (data != null && data.getLocalizedChoices() != null) {
			localizedChoices = data.getLocalizedChoices();
		} else {
			localizedChoices = SlashUtils.getNotLocalizedChoices(context, guild, info);
		}

		return new LocalizedCommandData(localizedPath, localizedDescription, localizedOptions, localizedChoices);
	}

	@NotNull
	public CommandPath getLocalizedPath() {
		return localizedPath;
	}

	@NotNull
	public String getLocalizedDescription() {
		return localizedDescription;
	}

	@NotNull
	public List<LocalizedOption> getLocalizedOptionNames() {
		return localizedOptionNames;
	}

	@NotNull
	public List<List<SlashCommand.Choice>> getLocalizedOptionChoices() {
		return localizedOptionChoices;
	}

	public static final class LocalizedOption {
		@NotNull private final String name, description;

		public LocalizedOption(@NotNull String name, @NotNull String description) {
			this.name = name;
			this.description = description;
		}

		@NotNull
		public String getName() {
			return name;
		}

		@NotNull
		public String getDescription() {
			return description;
		}
	}
}
