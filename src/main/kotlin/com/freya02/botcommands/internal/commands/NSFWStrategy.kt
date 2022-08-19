package com.freya02.botcommands.internal.commands

class NSFWStrategy internal constructor(val allowedInGuilds: Boolean, val allowInDMs: Boolean) {
    init {
        require(allowInDMs || allowedInGuilds) { "Cannot disable both guild and DMs NSFW, as it would disable the command permanently" }
    }
}