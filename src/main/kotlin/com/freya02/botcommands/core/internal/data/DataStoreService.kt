package com.freya02.botcommands.core.internal.data

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.core.api.annotations.ConditionalService
import com.freya02.botcommands.core.internal.db.Database
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.Utils
import kotlinx.coroutines.*
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@ConditionalService
internal class DataStoreService(
    private val database: Database,
    private val handlerContainer: DataStoreHandlerContainer,
    private val context: BContext
) {
    private val logger = Logging.getLogger()

    init {
        database.transactional {
            preparedStatement("delete from bc_data where lifetime_type = ?") {
                runBlocking { executeUpdate(LifetimeType.EPHEMERAL.id) }
            }

            preparedStatement("delete from bc_data where expiration_timestamp < now()") {
                runBlocking { executeUpdate(*emptyArray<Any>()) }
            }

            preparedStatement("select id, (extract(epoch from expiration_timestamp - now()) * 1000)::bigint as delayMilliseconds, timeout_handler_id from bc_data") {
                runBlocking {
                    executeQuery(*emptyArray<Any>()).forEach {
                        val dataId: String = it["id"]
                        val delayMilliseconds: Long = it["delayMilliseconds"]

                        scheduleDataTimeout(dataId, delayMilliseconds.milliseconds)
                    }
                }
            }
        }
    }

    suspend fun getData(id: String): DataEntity? = database.transactional {
        preparedStatement("select * from bc_data where id = ? limit 1") {
            val result = executeQuery(*arrayOf(id)).readOnce() ?: return@preparedStatement null

            DataEntity.fromDBResult(result)
        }
    }

    suspend fun putData(entity: PartialDataEntity): String {
        for (count in 1 .. 10) {
            try {
                return database.transactional<String> {
                    val id = Utils.randomId(64)

                    preparedStatement(
                        """
                        insert into bc_data (id, data, lifetime_type, expiration_timestamp, timeout_handler_id)
                        VALUES (?, ?, ?, ?, ?) returning id;""".trimIndent()
                    ) {
                        executeQuery(
                            id,
                            entity.data,
                            entity.lifetimeType.id,
                            entity.expirationTimestamp?.let { Timestamp.valueOf(it) },
                            entity.timeoutHandlerId
                        ).readOnce()!!["id"]
                    }
                }.also { dataId ->
                    entity.expirationTimestamp?.let {
                        CoroutineScope(currentCoroutineContext()).scheduleDataTimeout(
                            dataId,
                            entity.expirationTimestamp.toEpochSecond(ZoneOffset.UTC).seconds - LocalDateTime.now()
                                .toEpochSecond(ZoneOffset.UTC).seconds
                        )
                    }
                }
            } catch (e: Throwable) {
                if (count == 10) throw e
            }
        }

        throwInternal("Unable to insert in data store after 10 tries, no exceptions have been caught")
    }

    private fun CoroutineScope.scheduleDataTimeout(dataId: String, delay: Duration) = launch(Dispatchers.IO) {
        delay(delay)

        when (val data = getData(dataId)) {
            null -> logger.trace("Data not found for ID '$dataId'") //Might be normal if it was cleanup up by the user
            else -> data.let { dataEntity ->
                val timeoutHandler = handlerContainer.timeoutHandlers[data.timeoutHandlerId] ?: let {
                    logger.warn("No timeout handler found for '${data.timeoutHandlerId}'")

                    return@launch
                }

                runCatching {
                    timeoutHandler.execute(dataEntity)
                }.onFailure { e ->
                    logger.error("An exception occurred while running a data entity timeout handler, '$dataId'", e)

                    context.dispatchException("An exception occurred while running a data entity timeout handler", e)
                }
            }
        }
    }
}