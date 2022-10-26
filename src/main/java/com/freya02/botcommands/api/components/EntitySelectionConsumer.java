package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.components.event.EntitySelectionEvent;

@FunctionalInterface
public interface EntitySelectionConsumer extends SelectionConsumer<EntitySelectionEvent> {
}
