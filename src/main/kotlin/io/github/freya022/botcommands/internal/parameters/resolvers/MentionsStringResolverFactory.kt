package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.commands.application.slash.annotations.MentionsString
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.core.reflect.findAnnotation
import io.github.freya022.botcommands.api.core.reflect.requireUser
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.isAssignableFrom
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.api.parameters.ResolverRequest
import io.github.freya022.botcommands.api.parameters.TypedParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.internal.core.entities.InputUserImpl
import io.github.freya022.botcommands.internal.utils.*
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.SlashCommandReference
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.safeCast
import kotlin.reflect.typeOf

@ResolverFactory
internal object MentionsStringResolverFactory : ParameterResolverFactory<MentionsStringResolverFactory.MentionsStringResolver>(MentionsStringResolver::class) {
    internal class MentionsStringResolver private constructor(
        private val mentionTypes: Array<out MentionType>,
        private val transform: (IMentionable) -> IMentionable?
    ) : TypedParameterResolver<MentionsStringResolver, List<IMentionable>>(typeOf<List<IMentionable>>()),
        SlashParameterResolver<MentionsStringResolver, List<IMentionable>> {

        override val optionType: OptionType = OptionType.STRING

        override suspend fun resolveSuspend(
            info: SlashCommandInfo,
            event: CommandInteractionPayload,
            optionMapping: OptionMapping
        ): List<IMentionable> = optionMapping.mentions.getMentions(*mentionTypes).mapNotNull(transform)

        internal companion object {
            internal fun ofMentionable(mentionTypes: Array<out MentionType>): MentionsStringResolver {
                return MentionsStringResolver(mentionTypes) { it }
            }

            internal fun ofEntity(entityType: KClass<out IMentionable>, mentionType: MentionType) : MentionsStringResolver {
                return ofEntity(mentionType) { entityType.safeCast(it) }
            }

            internal fun ofEntity(mentionType: MentionType, mapper: (IMentionable) -> IMentionable?) : MentionsStringResolver {
                return MentionsStringResolver(arrayOf(mentionType), mapper)
            }
        }
    }

    override val supportedTypesStr: List<String> = listOf(IMentionable::class.shortQualifiedName)

    override fun isResolvable(request: ResolverRequest): Boolean {
        val parameter = request.parameter
        val annotation = parameter.findAnnotation<MentionsString>() ?: return false
        // Must be a List
        parameter.requireUser(parameter.erasure.isSubclassOf<List<*>>()) {
            "Parameter '${parameter.name}' annotated with ${annotationRef<MentionsString>()} must be a ${classRef<List<*>>()} subtype"
        }

        // Elements must be mentionable
        val elementType = parameter.type.findErasureOfAt<List<*>>(0).jvmErasure
        require(IMentionable::class.isAssignableFrom(elementType)) {
            "Parameter '${parameter.name}' annotated with ${annotationRef<MentionsString>()} must be a ${classRef<List<*>>()} containing ${classRef<IMentionable>()} or any subtype"
        }

        // If they are not mentionable, the annotation must not have any mention type
        if (elementType != IMentionable::class) {
            // If this is a concrete type, do not allow types
            parameter.requireUser(annotation.types.isEmpty()) {
                "Parameter '${parameter.name}' annotated with ${annotationRef<MentionsString>()} cannot have mention types on a concrete list element type (${elementType.simpleNestedName})"
            }
        }

        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(request: ResolverRequest): MentionsStringResolver {
        val parameter = request.parameter
        val annotation = parameter.findAnnotation<MentionsString>()
            ?: throwInternal("Missing ${annotationRef<MentionsString>()}")

        val elementErasure = parameter.type.findErasureOfAt<List<*>>(0).jvmErasure as KClass<out IMentionable>
        return if (elementErasure == IMentionable::class) {
            MentionsStringResolver.ofMentionable(annotation.types.ifEmpty { MentionType.entries.toTypedArray() })
        } else if (elementErasure == User::class) {
            MentionsStringResolver.ofEntity(MentionType.USER) {
                when (it) {
                    is Member -> it.user
                    is User -> it
                    else -> null
                }
            }
        } else if (elementErasure == Member::class) {
            MentionsStringResolver.ofEntity(elementErasure, MentionType.USER)
        } else if (elementErasure == InputUser::class) {
            MentionsStringResolver.ofEntity(MentionType.USER) {
                when (it) {
                    is Member -> InputUserImpl(it)
                    is User -> InputUserImpl(it)
                    else -> null
                }
            }
        } else if (elementErasure.isSubclassOf<GuildChannel>()) {
            MentionsStringResolver.ofEntity(elementErasure, MentionType.CHANNEL)
        } else if (elementErasure == Role::class) {
            MentionsStringResolver.ofEntity(elementErasure, MentionType.ROLE)
        } else if (elementErasure == CustomEmoji::class) {
            MentionsStringResolver.ofEntity(elementErasure, MentionType.EMOJI)
        } else if (elementErasure == SlashCommandReference::class) {
            MentionsStringResolver.ofEntity(elementErasure, MentionType.SLASH_COMMAND)
        } else {
            throwUser("Unsupported element type: ${elementErasure.shortQualifiedName}")
        }
    }
}