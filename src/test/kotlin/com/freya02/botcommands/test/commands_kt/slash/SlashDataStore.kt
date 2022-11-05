package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.internal.data.DataEntity
import com.freya02.botcommands.internal.data.DataEntityTimeout
import com.freya02.botcommands.internal.data.DataStoreService
import com.freya02.botcommands.internal.data.PartialDataEntity
import com.freya02.botcommands.internal.data.annotations.DataStoreTimeoutHandler
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlin.time.Duration.Companion.seconds

@CommandMarker
class SlashDataStore : ApplicationCommand() {
    @CommandMarker //Just a test
    internal suspend fun onSlashDataStorePut(event: GuildSlashEvent, dataStore: DataStoreService) {
        val id = dataStore.putData(PartialDataEntity.ofEphemeral(mapOf("bruh" to 42), DataEntityTimeout(5.seconds, "timeout_handler1")))

        event.reply_("id: $id", ephemeral = true).queue()
    }

    @CommandMarker //Just a test
    internal suspend fun onSlashDataStoreGet(event: GuildSlashEvent, id: String, dataStore: DataStoreService) {
        val data = dataStore.getData(id)
        val map = data?.decodeData<Map<String, *>>()

        event.deferReply().await()

        println(data)
        println(map)

        event.hook.send("ok", ephemeral = true).queue()
    }

    @DataStoreTimeoutHandler("timeout_handler1")
    internal fun onTimeout(dataEntity: DataEntity) {
        println(dataEntity)
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("data_store") {
            subcommand("put") {
                customOption("dataStore")

                function = ::onSlashDataStorePut
            }

            subcommand("get") {
                option("id")

                customOption("dataStore")

                function = ::onSlashDataStoreGet
            }
        }
    }
}