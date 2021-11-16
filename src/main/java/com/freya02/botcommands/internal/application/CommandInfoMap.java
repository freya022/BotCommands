package com.freya02.botcommands.internal.application;

import com.freya02.botcommands.api.application.CommandPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass", "SuspiciousMethodCalls"})
public class CommandInfoMap<T extends ApplicationCommandInfo> implements Map<CommandPath, T> {
	private final Map<CommandPath, T> map;

	public CommandInfoMap() {
		map = new HashMap<>();
	}

	public CommandInfoMap(Map<CommandPath, T> map) {
		this.map = map;
	}

	@UnmodifiableView
	public CommandInfoMap<T> unmodifiable() {
		return new CommandInfoMap<>(Collections.unmodifiableMap(map));
	}

	@Override
	public int size() {return map.size();}

	@Override
	public boolean isEmpty() {return map.isEmpty();}

	@Override
	public boolean containsKey(Object key) {return map.containsKey(key);}

	@Override
	public boolean containsValue(Object value) {return map.containsValue(value);}

	@Override
	public T get(Object key) {return map.get(key);}

	@Nullable
	@Override
	public T put(CommandPath key, T value) {return map.put(key, value);}

	@Override
	public T remove(Object key) {return map.remove(key);}

	@Override
	public void putAll(@NotNull Map<? extends CommandPath, ? extends T> m) {map.putAll(m);}

	@Override
	public void clear() {map.clear();}

	@NotNull
	@Override
	public Set<CommandPath> keySet() {return map.keySet();}

	@NotNull
	@Override
	public Collection<T> values() {return map.values();}

	@NotNull
	@Override
	public Set<Entry<CommandPath, T>> entrySet() {return map.entrySet();}

	@Override
	public boolean equals(Object o) {return map.equals(o);}

	@Override
	public int hashCode() {return map.hashCode();}

	@Override
	public T getOrDefault(Object key, T defaultValue) {return map.getOrDefault(key, defaultValue);}

	@Override
	public void forEach(BiConsumer<? super CommandPath, ? super T> action) {map.forEach(action);}

	@Override
	public void replaceAll(BiFunction<? super CommandPath, ? super T, ? extends T> function) {map.replaceAll(function);}

	@Nullable
	@Override
	public T putIfAbsent(CommandPath key, T value) {return map.putIfAbsent(key, value);}

	@Override
	public boolean remove(Object key, Object value) {return map.remove(key, value);}

	@Override
	public boolean replace(CommandPath key, T oldValue, T newValue) {return map.replace(key, oldValue, newValue);}

	@Nullable
	@Override
	public T replace(CommandPath key, T value) {return map.replace(key, value);}

	@Override
	public T computeIfAbsent(CommandPath key, @NotNull Function<? super CommandPath, ? extends T> mappingFunction) {return map.computeIfAbsent(key, mappingFunction);}

	@Override
	public T computeIfPresent(CommandPath key, @NotNull BiFunction<? super CommandPath, ? super T, ? extends T> remappingFunction) {return map.computeIfPresent(key, remappingFunction);}

	@Override
	public T compute(CommandPath key, @NotNull BiFunction<? super CommandPath, ? super T, ? extends T> remappingFunction) {return map.compute(key, remappingFunction);}

	@Override
	public T merge(CommandPath key, @NotNull T value, @NotNull BiFunction<? super T, ? super T, ? extends T> remappingFunction) {return map.merge(key, value, remappingFunction);}
}
