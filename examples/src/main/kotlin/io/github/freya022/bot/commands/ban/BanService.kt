package io.github.freya022.bot.commands.ban

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.DynamicSupplier
import com.freya02.botcommands.api.core.service.DynamicSupplier.Instantiability
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import kotlin.reflect.KClass

@BService // Just a marker, not required
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
@BService
@ServiceType(DynamicSupplier::class)
class BanServiceSupplier : DynamicSupplier {
    private val useSql = true

    // Determining if this dynamic supplier is capable of giving an instance of the provided class
    override fun getInstantiability(context: BContext, clazz: KClass<*>): Instantiability {
        if (clazz == BanService::class) {
            return Instantiability.instantiable()
        }

        return Instantiability.unsupportedType()
    }

    // Only called if the above function returned class as instantiable
    override fun get(context: BContext, clazz: KClass<*>) = when {
        useSql -> SqlBanService()
        else -> JsonBanService()
    }
}