package com.freya02.botcommands.test.commands2

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import com.freya02.botcommands.core.internal.data.DataEntity
import com.freya02.botcommands.core.internal.data.DataStoreService
import com.freya02.botcommands.core.internal.data.PartialDataEntity
import com.freya02.botcommands.core.internal.data.annotations.DataStoreTimeoutHandler
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import kotlin.time.Duration.Companion.seconds

@CommandMarker
class SlashDataStore : ApplicationCommand() {
    @CommandMarker //Just a test
    internal suspend fun onSlashDataStorePut(event: GuildSlashEvent, dataStore: DataStoreService) {
        val id = dataStore.putData(PartialDataEntity.ofEphemeral("""{"bruh": 42}""", 5.seconds, "timeout_handler1"))

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

    @Declaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("data_store", subcommand = "put") {
            customOption("dataStore")

            function = ::onSlashDataStorePut
        }

        globalApplicationCommandManager.slashCommand("data_store", subcommand = "get") {
            option("id")

            customOption("dataStore")

            function = ::onSlashDataStoreGet
        }
    }
}