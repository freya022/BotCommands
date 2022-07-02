package com.freya02.botcommands.internal

import net.dv8tion.jda.api.entities.Guild

fun Guild?.asScopeString() = if (this == null) "global scope" else "guild '${this.name}' (${this.id})"