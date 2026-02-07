package dev.freya02.botcommands.jda.keepalive.internal

import io.github.freya022.botcommands.api.core.utils.enumMapOf
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.core.utils.enumSetOfAll
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.hooks.InterfacedEventManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.util.*

private val logger = KotlinLogging.logger { }

class JDABuilderConfiguration internal constructor() {

    private val warnedUnsupportedValues: MutableSet<String> = hashSetOf()

    var hasUnsupportedValues = false
        private set

    private val builderValues: MutableMap<ValueType, Any?> = enumMapOf()
    private var _eventManager: IEventManager? = null
    val eventManager: IEventManager get() = _eventManager ?: InterfacedEventManager()

    // So we can track the initial token and intents, the constructor will be instrumented and call this method
    // The user overriding the values using token/intent setters should not be an issue
    @DynamicCall
    fun onInit(token: String?, intents: Int) {
        builderValues[ValueType.TOKEN] = token
        builderValues[ValueType.INTENTS] = intents
        builderValues[ValueType.CACHE_FLAGS] = enumSetOfAll<CacheFlag>()
    }

    @DynamicCall
    fun markUnsupportedValue(signature: String) {
        if (warnedUnsupportedValues.add(signature))
            logger.warn { "Unsupported JDABuilder method '$signature', JDA will not be cached between restarts" }
        hasUnsupportedValues = true
    }

    @DynamicCall
    fun setStatus(status: OnlineStatus) {
        builderValues[ValueType.STATUS] = status
    }

    @DynamicCall
    fun setEventManager(eventManager: IEventManager?) {
        _eventManager = eventManager
    }

    @DynamicCall
    fun setEventPassthrough(enable: Boolean) {
        builderValues[ValueType.EVENT_PASSTHROUGH] = enable
    }

    @DynamicCall
    @Suppress("UNCHECKED_CAST")
    fun enableCache(first: CacheFlag, vararg others: CacheFlag) {
        (builderValues[ValueType.CACHE_FLAGS] as EnumSet<CacheFlag>) += enumSetOf(first, *others)
    }

    @DynamicCall
    @Suppress("UNCHECKED_CAST")
    fun enableCache(flags: Collection<CacheFlag>) {
        (builderValues[ValueType.CACHE_FLAGS] as EnumSet<CacheFlag>) += flags
    }

    @DynamicCall
    @Suppress("UNCHECKED_CAST")
    fun disableCache(first: CacheFlag, vararg others: CacheFlag) {
        (builderValues[ValueType.CACHE_FLAGS] as EnumSet<CacheFlag>) -= enumSetOf(first, *others)
    }

    @DynamicCall
    @Suppress("UNCHECKED_CAST")
    fun disableCache(flags: Collection<CacheFlag>) {
        (builderValues[ValueType.CACHE_FLAGS] as EnumSet<CacheFlag>) -= flags
    }

    @DynamicCall
    fun setMemberCachePolicy(memberCachePolicy: MemberCachePolicy?) {
        builderValues[ValueType.MEMBER_CACHE_POLICY] = memberCachePolicy
    }

    @DynamicCall
    fun setChunkingFilter(filter: ChunkingFilter?) {
        builderValues[ValueType.CHUNKING_FILTER] = filter
    }

    @DynamicCall
    fun setLargeThreshold(threshold: Int) {
        builderValues[ValueType.LARGE_THRESHOLD] = threshold
    }

    @DynamicCall
    fun setActivity(activity: Activity?) {
        builderValues[ValueType.ACTIVITY] = activity
    }

    @DynamicCall
    fun setEnableShutdownHook(enable: Boolean) {
        builderValues[ValueType.ENABLE_SHUTDOWN_HOOK] = enable
    }

    internal infix fun isSameAs(other: JDABuilderConfiguration): Boolean {
        if (hasUnsupportedValues) return false
        if (other.hasUnsupportedValues) return false

        return builderValues == other.builderValues
    }

    private enum class ValueType {
        TOKEN,
        INTENTS,
        STATUS,
        EVENT_PASSTHROUGH,
        CACHE_FLAGS,
        // These two are interfaces, it's fine to compare them by equality,
        // their reference will be the same as they are from the app class loader,
        // so if two runs uses MemberCachePolicy#VOICE, it'll still be compatible
        MEMBER_CACHE_POLICY,
        CHUNKING_FILTER,
        LARGE_THRESHOLD,
        ACTIVITY,
        ENABLE_SHUTDOWN_HOOK,
    }
}
