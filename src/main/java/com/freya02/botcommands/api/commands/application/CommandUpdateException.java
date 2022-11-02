package com.freya02.botcommands.api.commands.application;

import kotlin.reflect.KFunction;

public record CommandUpdateException(KFunction<?> function, Throwable throwable) {}
