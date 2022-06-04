package com.freya02.botcommands.test

import com.freya02.botcommands.api.CommandsBuilder
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.reflectReference
import com.freya02.botcommands.test.commands2.MyCommand
import com.freya02.botcommands.test.commands2.MyJavaCommand
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

fun KFunction<*>.getNonInstanceParams() = this.parameters.filter { param -> param.kind != KParameter.Kind.INSTANCE }

class Main {
    fun func() {

    }

    fun acc(f: KFunction<*>) {}

    fun app(path: CommandPath, instance: Any, builder: SlashCommandBuilder.() -> Unit): SlashCommandBuilder =
        SlashCommandBuilder(BContextImpl(), path).apply(builder)

    fun bruh() {
        val inst = MyCommand()

//        val builder = app(CommandPath.of("lol"), this) {
//
//        }

        val function = inst::executeCommand
        acc(function)
        acc(::func)

        val reflected = function.reflectReference()

        println()
    }
}

fun main() {
    //val builder2 = SlashCommandBuilder(CommandPath.of("lol")).apply {
    //    function = ::func
    //}

    val f1 = MyCommand::executeCommand
    val f2 = MyJavaCommand::cmd

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