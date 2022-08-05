package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.api.application.CommandPath;
import org.jetbrains.annotations.NotNull;

public interface IHelpCommand {
	void onInvalidCommand(@NotNull BaseCommandEvent event, @NotNull CommandPath executedCommandPath);
}
