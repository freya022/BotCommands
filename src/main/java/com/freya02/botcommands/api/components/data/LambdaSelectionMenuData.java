package com.freya02.botcommands.api.components.data;

import com.freya02.botcommands.api.components.SelectionConsumer;
import org.jetbrains.annotations.NotNull;

public record LambdaSelectionMenuData(@NotNull SelectionConsumer consumer) {}
