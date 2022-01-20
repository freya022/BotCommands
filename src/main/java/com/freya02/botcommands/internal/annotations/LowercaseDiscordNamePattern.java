package com.freya02.botcommands.internal.annotations;

import org.intellij.lang.annotations.Pattern;

@Pattern("[a-z0-9_-]+")
public @interface LowercaseDiscordNamePattern {}