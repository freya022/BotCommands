package com.freya02.botcommands.components.builder;

import java.util.List;

public interface PersistentComponentBuilder {
	String getHandlerName();

	List<String> getArgs();
}
