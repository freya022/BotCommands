package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.api.application.slash.DefaultValue;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConstantDefaultOptionResolver implements DefaultOptionResolver {
	private final SlashCommandInfo commandInfo;
	private final SlashCommandParameter slashCommandParameter;

	private final TLongObjectMap<DefaultValue> valueMap = new TLongObjectHashMap<>();

	public ConstantDefaultOptionResolver(@NotNull SlashCommandInfo commandInfo, @NotNull SlashCommandParameter slashCommandParameter) {
		this.commandInfo = commandInfo;
		this.slashCommandParameter = slashCommandParameter;
	}

	@Override
	@Nullable
	public DefaultValue resolve(@NotNull BContext context, @NotNull Guild guild) {
		DefaultValue value = valueMap.get(guild.getIdLong());

		if (value == null) {
			synchronized (valueMap) {
				if (valueMap.get(guild.getIdLong()) == null) {
					value = getDefaultValue(context, guild);

					valueMap.put(guild.getIdLong(), value);
				}
			}
		}

		return value;
	}

	@Nullable
	private DefaultValue getDefaultValue(@NotNull BContext context, @NotNull Guild guild) {
		DefaultValue value = commandInfo.getInstance().getDefaultValue(context,
				guild,
				commandInfo.getCommandId(),
				commandInfo.getPath(),
				slashCommandParameter.getApplicationOptionData().getEffectiveName(),
				slashCommandParameter.getBoxedType()
		);

		if (value == null) {
			final SettingsProvider settingsProvider = context.getSettingsProvider();

			if (settingsProvider != null) {
				return settingsProvider.getDefaultValue(context,
						guild,
						commandInfo.getCommandId(),
						commandInfo.getPath(),
						slashCommandParameter.getApplicationOptionData().getEffectiveName(),
						slashCommandParameter.getBoxedType()
				);
			}
		}

		return value;
	}
}
