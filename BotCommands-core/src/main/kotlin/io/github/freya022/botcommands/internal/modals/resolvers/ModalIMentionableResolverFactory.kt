package io.github.freya022.botcommands.internal.modals.resolvers

import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.entities.inputUsers
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.options.ModalOption
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import io.github.freya022.botcommands.internal.modals.resolvers.ModalIMentionableResolverFactory.IMentionableResolver
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.collectionElementType
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.throwArgument
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

@ResolverFactory
internal object ModalIMentionableResolverFactory :
        ParameterResolverFactory<IMentionableResolver>(IMentionableResolver::class) {

    override val supportedTypesStr: List<String> = listOf(
        "<out ${IMentionable::class.shortQualifiedName}>",
        "List<${IMentionable::class.shortQualifiedName}>"
    )

    override val supportedResolvers = inferSupportedResolversFrom<IMentionableResolver>()

    override fun isResolvable(request: ResolverRequest): Boolean {
        // Only support lists
        val rawErasure = request.parameter.erasure
        if (rawErasure.isSubclassOf<Collection<*>>() && rawErasure.java != List::class.java) {
            return false
        }

        val effectiveElementErasure = getEffectiveElementType(request).jvmErasure
        return effectiveElementErasure.isSubclassOf<IMentionable>()
    }

    override fun get(request: ResolverRequest): IMentionableResolver {
        val rawType = request.parameter.type
        val effectiveElementType = getEffectiveElementType(request)
        return when (effectiveElementType.jvmErasure) {
            IMentionable::class -> IMentionableResolver(rawType, Mentions::getMentions)
            User::class -> IMentionableResolver(rawType, Mentions::getUsers)
            Member::class -> IMentionableResolver(rawType, Mentions::getMembers)
            InputUser::class -> IMentionableResolver(rawType, Mentions::inputUsers)
            Role::class -> IMentionableResolver(rawType, Mentions::getRoles)
            GuildChannel::class -> IMentionableResolver(rawType, Mentions::getChannels)
            else -> throwArgument("Unsupported mentionable type: ${effectiveElementType.shortQualifiedName}")
        }
    }

    private fun getEffectiveElementType(request: ResolverRequest): KType {
        val type = request.parameter.type
        return if (type.jvmErasure.isSubclassOf<Collection<*>>()) {
            type.collectionElementType!!
        } else {
            type
        }
    }

    internal class IMentionableResolver(
        type: KType,
        private val mentionSupplier: (Mentions) -> List<IMentionable>,
    ) : TypedParameterResolver<IMentionableResolver, Any>(type),
        ModalParameterResolver<IMentionableResolver, Any> {

        private val isSingle = !type.jvmErasure.isSubclassOf<Collection<*>>()

        override suspend fun resolveSuspend(option: ModalOption, event: ModalEvent, modalMapping: ModalMapping): Any? {
            val mentions = mentionSupplier(modalMapping.asMentions)
            if (isSingle) {
                if (mentions.size > 1) {
                    throwArgument(
                        option.kParameter.function,
                        "Cannot return a single ${type.jvmErasure.simpleName} from a select menu with multiple selections"
                    )
                }

                return mentions.firstOrNull()
            } else {
                return mentions
            }
        }
    }
}
