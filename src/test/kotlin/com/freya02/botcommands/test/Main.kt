package com.freya02.botcommands.test

import com.freya02.botcommands.api.CommandsBuilder
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent

fun func() {

}

fun main() {
    //val builder2 = SlashCommandBuilder(CommandPath.of("lol")).apply {
    //    function = ::func
    //}

//    val kFunction1 = KtTest::lol

    val config = Config.readConfig()

    val jda = JDABuilder.createLight(config.token)
        .enableIntents(GatewayIntent.GUILD_MEMBERS)
        .setActivity(Activity.playing("coroutines go brrr"))
        .build()
        .awaitReady()

    CommandsBuilder.newBuilder()
        .textCommandBuilder {
            it.disableHelpCommand { _, _ ->  }
        }
        .addSearchPath("com.freya02.botcommands.test.commands2")
        .build(jda)

    println()
}