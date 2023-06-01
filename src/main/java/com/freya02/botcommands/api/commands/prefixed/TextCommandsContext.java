package com.freya02.botcommands.api.commands.prefixed;

import com.freya02.botcommands.api.core.service.annotations.InjectedService;
import com.freya02.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo;

import java.util.Collection;

@InjectedService
public interface TextCommandsContext {
	Collection<TopLevelTextCommandInfo> getRootCommands();
}