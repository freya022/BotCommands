package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandOption

interface ApplicationCommandOption : CommandOption {
    override val command: ApplicationCommandInfo
}