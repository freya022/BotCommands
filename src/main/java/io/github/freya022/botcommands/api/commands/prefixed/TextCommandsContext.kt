package io.github.freya022.botcommands.api.commands.prefixed;

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService;
import io.github.freya022.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo;

import java.util.Collection;

@InjectedService
public interface TextCommandsContext {
	Collection<TopLevelTextCommandInfo> getRootCommands();
}