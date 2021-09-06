package com.freya02.botcommands.application;

import net.dv8tion.jda.api.interactions.commands.SlashCommand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class LocalizedCommandData {
	public static final class LocalizedOption {
		private final String name, description;

		public LocalizedOption(@Nonnull String name, @Nonnull String description) {
			this.name = name;
			this.description = description;
		}

		@Nonnull
		public String getName() {
			return name;
		}

		@Nonnull
		public String getDescription() {
			return description;
		}
	}

	@Nullable private final CommandPath localizedPath;
	@Nullable private final String localizedDescription;
	@Nullable private final List<LocalizedOption> localizedOptionNames;
	@Nullable private final List<List<SlashCommand.Choice>> localizedOptionChoices;

	/**
	 * Constructs new localized application command data for a specific Guild
	 *
	 * @param localizedPath          The path of the command, this should look like this <code>name/subgroup/subcommand</code>
	 * @param localizedDescription   The description of the command, <i>not used for context commands</i>
	 * @param localizedOptionNames   The list of option names (parameters) of the command, <i>not used for context commands</i>
	 * @param localizedOptionChoices The list of choices for all the command options, each entry of this list is the choices for one parameter, <i>not used for context commands</i>
	 *                               <br> <i>Choice list n°0 is the choice list for option n°0 and such</i>
	 */
	public LocalizedCommandData(@Nullable String localizedPath,
	                            @Nullable String localizedDescription,
	                            @Nullable List<LocalizedOption> localizedOptionNames,
	                            @Nullable List<List<SlashCommand.Choice>> localizedOptionChoices) {
		this.localizedPath = localizedPath == null ? null : CommandPath.of(localizedPath);
		this.localizedDescription = localizedDescription;
		this.localizedOptionNames = localizedOptionNames;
		this.localizedOptionChoices = localizedOptionChoices;
	}

	@Nullable
	public CommandPath getLocalizedPath() {
		return localizedPath;
	}

	@Nullable
	public String getLocalizedDescription() {
		return localizedDescription;
	}

	@Nullable
	public List<LocalizedOption> getLocalizedOptionNames() {
		return localizedOptionNames;
	}

	@Nullable
	public List<List<SlashCommand.Choice>> getLocalizedOptionChoices() {
		return localizedOptionChoices;
	}
}
