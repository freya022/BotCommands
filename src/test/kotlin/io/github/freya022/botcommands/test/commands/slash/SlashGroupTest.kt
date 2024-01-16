package io.github.freya022.botcommands.test.commands.slash

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashCommandGroupData
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData

@Command
class SlashGroupTest : ApplicationCommand() {
    @TopLevelSlashCommandData
    @SlashCommandGroupData(description = "group desc")
    @JDASlashCommand(name = "group_test", group = "group1", subcommand = "sub")
    fun onSlashGroup1Test(event: GuildSlashEvent) {}

//    @TopLevelSlashCommandData
//    @SlashCommandGroupData(description = "group desc")
    @JDASlashCommand(name = "group_test", group = "group1", subcommand = "sub2")
    fun onSlashGroup1Test2(event: GuildSlashEvent) {}

    @JDASlashCommand(name = "group_test", group = "group2", subcommand = "sub")
    fun onSlashGroup2Test(event: GuildSlashEvent) {}

    @JDASlashCommand(name = "group_test", group = "group3", subcommand = "sub")
    fun onSlashGroup3Test(event: GuildSlashEvent) {}
}