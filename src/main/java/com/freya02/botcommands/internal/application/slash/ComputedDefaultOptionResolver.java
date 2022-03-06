package com.freya02.botcommands.internal.application.slash;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.slash.DefaultValue;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComputedDefaultOptionResolver implements DefaultOptionResolver {
	private final SlashCommandInfo commandInfo;
	private final SlashCommandParameter slashCommandParameter;

	public ComputedDefaultOptionResolver(SlashCommandInfo commandInfo, SlashCommandParameter slashCommandParameter) {
		this.commandInfo = commandInfo;
		this.slashCommandParameter = slashCommandParameter;
	}

	@Override
	@Nullable
	public DefaultValue resolve(@NotNull BContext context, @NotNull Guild guild) {
		return commandInfo.getInstance().getConstantDefaultValue(context,
				guild,
				commandInfo.getCommandId(),
				commandInfo.getPath(),
				slashCommandParameter.getApplicationOptionData().getEffectiveName(),
				slashCommandParameter.getBoxedType()
		);
	}
}
