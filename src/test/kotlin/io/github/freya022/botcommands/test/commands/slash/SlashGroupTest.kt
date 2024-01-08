package io.github.freya022.botcommands.test.commands.slash

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDATopLevelSlashCommand

@Command
class SlashGroupTest : ApplicationCommand() {
    @JDATopLevelSlashCommand
    @JDASlashCommand(name = "group_test", group = "group1", subcommand = "sub")
    fun onSlashGroup1Test(event: GuildSlashEvent) {}

    @JDASlashCommand(name = "group_test", group = "group2", subcommand = "sub")
    fun onSlashGroup2Test(event: GuildSlashEvent) {}

    @JDASlashCommand(name = "group_test", group = "group3", subcommand = "sub")
    fun onSlashGroup3Test(event: GuildSlashEvent) {}
}