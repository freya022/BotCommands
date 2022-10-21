package com.freya02.botcommands.api.components.builder.selects;

import com.freya02.botcommands.api.components.builder.ComponentBuilder;
import com.freya02.botcommands.api.components.builder.PersistentComponentBuilder;

public interface PersistentSelectionMenuBuilder<T extends PersistentSelectionMenuBuilder<T>>
		extends ComponentBuilder<T>,
		        PersistentComponentBuilder<T> {

}
