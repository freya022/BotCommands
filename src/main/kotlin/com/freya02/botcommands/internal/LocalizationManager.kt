package com.freya02.botcommands.internal

import com.freya02.botcommands.api.localization.annotations.LocalizationBundle
import com.freya02.botcommands.api.localization.annotations.LocalizationPrefix
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter

class LocalizationManager {
    private val prefixMap: MutableMap<KFunction<*>, String?> = Collections.synchronizedMap(mutableMapOf())
    private val bundleMap: MutableMap<KFunction<*>, String?> = Collections.synchronizedMap(mutableMapOf())

    fun getLocalizationPrefix(function: KFunction<*>): String? {
        return prefixMap.computeIfAbsent(function) {
            val methodPrefix = function.findAnnotation<LocalizationPrefix>()
            if (methodPrefix != null) return@computeIfAbsent methodPrefix.value

            val classPrefix = function.instanceParameter!!.type.findAnnotation<LocalizationPrefix>()
            if (classPrefix != null) return@computeIfAbsent classPrefix.value

            null
        }
    }

    fun getLocalizationBundle(function: KFunction<*>): String? {
        return bundleMap.computeIfAbsent(function) {
            val functionBundle = function.findAnnotation<LocalizationBundle>()
            if (functionBundle != null) return@computeIfAbsent functionBundle.value

            val classBundle = function.instanceParameter!!.type.findAnnotation<LocalizationBundle>()
            if (classBundle != null) return@computeIfAbsent classBundle.value

            null
        }
    }
}