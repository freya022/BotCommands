package io.github.freya022.botcommands.api.commands.application;

import kotlin.reflect.KFunction;

public record CommandUpdateException(KFunction<?> function, Throwable throwable) {}
