package io.github.freya022.botcommands.api.core.utils

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object DefaultObjectMapper {
    @get:JvmStatic
    val lock: ReentrantLock = ReentrantLock()

    @get:JvmStatic
    val mapper: ObjectMapper = jacksonObjectMapper()

    @get:JvmStatic
    val mapType: MapType

    @get:JvmStatic
    val listType: CollectionType

    init {
        val module = SimpleModule()
        module.addAbstractTypeMapping(Map::class.java, HashMap::class.java)
        module.addAbstractTypeMapping(List::class.java, ArrayList::class.java)
        mapper.registerModule(module)
        mapper.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature())

        mapType = mapper.typeFactory.constructRawMapType(HashMap::class.java)
        listType = mapper.typeFactory.constructRawCollectionType(ArrayList::class.java)
    }

    @JvmStatic
    fun readMap(input: ByteArray): Map<String, *> {
        return lock.withLock { mapper.readValue(input, mapType) }
    }

    @JvmStatic
    fun readMap(input: String): Map<String, *> {
        return lock.withLock { mapper.readValue(input, mapType) }
    }

    @JvmStatic
    fun readMap(input: InputStream): Map<String, *> {
        return lock.withLock { mapper.readValue(input, mapType) }
    }

    @JvmStatic
    fun readList(input: ByteArray): List<*> {
        return lock.withLock { mapper.readValue(input, listType) }
    }

    @JvmStatic
    fun readList(input: String): List<*> {
        return lock.withLock { mapper.readValue(input, listType) }
    }

    @JvmStatic
    fun readList(input: InputStream): List<*> {
        return lock.withLock { mapper.readValue(input, listType) }
    }
}
