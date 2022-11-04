package com.freya02.botcommands.internal.data.adapters

import com.google.gson.*
import gnu.trove.list.array.TLongArrayList
import java.lang.reflect.Type

internal object TLongArrayListAdapter : JsonSerializer<TLongArrayList>, JsonDeserializer<TLongArrayList> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TLongArrayList {
        val array = json.asJsonArray
        return TLongArrayList(array.size()).also { list ->
            for (element in array) {
                list.add(element.asLong)
            }
        }
    }

    override fun serialize(src: TLongArrayList, typeOfSrc: Type, context: JsonSerializationContext) =
        JsonArray(src.size()).also { array ->
            for (l in src) {
                array.add(l)
            }
        }
}