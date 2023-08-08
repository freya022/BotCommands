package com.freya02.botcommands.api.core.utils

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.MapType
import java.io.InputStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object DefaultObjectMapper {
    val lock: ReentrantLock = ReentrantLock()
    val mapper: ObjectMapper = ObjectMapper()
    val mapType: MapType
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
    fun readMap(input: InputStream): Map<String, *> {
        return lock.withLock { mapper.readValue(input, mapType) }
    }

    @JvmStatic
    fun readList(input: ByteArray): List<*> {
        return lock.withLock { mapper.readValue(input, listType) }
    }

    @JvmStatic
    fun readList(input: InputStream): List<*> {
        return lock.withLock { mapper.readValue(input, listType) }
    }
}