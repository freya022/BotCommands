package io.github.freya022.bot.commands.ban

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

// Services that implement this interface will automatically be registered as this type,
// in addition to the determined service type
@InterfacedService(acceptMultiple = false)
interface BanService {
    suspend fun insertBan()
    suspend fun removeBan()
}

class SqlBanService : BanService {
    override suspend fun insertBan(): Unit = TODO("Not yet implemented")
    override suspend fun removeBan(): Unit = TODO("Not yet implemented")
}

//Just for the example, using a JSON for any kind of storage a bad due to possible data loss and lack of structure
class JsonBanService : BanService {
    override suspend fun insertBan(): Unit = TODO("Not yet implemented")
    override suspend fun removeBan(): Unit = TODO("Not yet implemented")
}

// This class is just an example on how you can provide services based off a strategy,
// i.e., giving a different implementation based on some parameters, while using the same interface.
@Suppress("unused")
object BanServiceSupplier {
    private const val USE_SQL = true

    @BService
    fun get(): BanService = when {
        USE_SQL -> SqlBanService()
        else -> JsonBanService()
    }
}