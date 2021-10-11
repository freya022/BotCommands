package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.CommandPath;
import net.dv8tion.jda.api.interactions.commands.CommandType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ApplicationCommandMap {
	//The String is CommandPath's base name
	private final EnumMap<CommandType, Map<String, CommandData>> typeMap = new EnumMap<>(CommandType.class);
	
	public Collection<CommandData> getAllCommandData() {
		return typeMap.values()
				.stream()
				.flatMap(map -> map.values().stream()).toList();
	}
	
	public CommandData computeIfAbsent(CommandType type, CommandPath path, Function<String, CommandData> mappingFunction) {
		return getTypeMap(type).computeIfAbsent(path.getName(), mappingFunction);
	}

	public CommandData put(CommandType type, CommandPath path, CommandData value) {
		return getTypeMap(type).put(path.getName(), value);
	}

	private Map<String, CommandData> getTypeMap(CommandType type) {
		return typeMap.computeIfAbsent(type, x -> new HashMap<>());
	}
}
