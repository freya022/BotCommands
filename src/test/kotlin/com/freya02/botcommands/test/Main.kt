package com.freya02.botcommands.test

import com.freya02.botcommands.api.CommandsBuilder
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.reflectReference
import com.freya02.botcommands.test.commands2.MyCommand
import com.freya02.botcommands.test.commands2.MyJavaCommand
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.getDefaultScope
import dev.minn.jda.ktx.jdabuilder.light
import kotlinx.coroutines.cancel
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.time.Duration.Companion.minutes

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

    val scope = getDefaultScope()
    val manager = CoroutineEventManager(scope, 1.minutes)
    manager.listener<ShutdownEvent> {
        scope.cancel()
    }

    CommandsBuilder.newBuilder()
        .textCommandBuilder {
            it.disableHelpCommand { _, _ ->  }
        }
        .addSearchPath("com.freya02.botcommands.test.commands2")
        .build()

    //CommandsBuilder would have a "jdaBuilder" method to configure JDABuilder
    // The JDABuilder may: prevent event manager assignment, or, construct a delegate from the set manager + coro manager
    // Build should return a BContext
    val jda = light(config.token, enableCoroutines = false) {
        enableIntents(GatewayIntent.GUILD_MEMBERS)
        setActivity(Activity.playing("coroutines go brrr"))
        setEventManager(manager)
    }.awaitReady()

    println()
}