package dev.freya02.botcommands.jda.ktx

import dev.freya02.botcommands.jda.ktx.utils.findErasureOfAt
import dev.freya02.botcommands.jda.ktx.utils.getJavaInvocationParameterTypes
import dev.freya02.botcommands.jda.ktx.utils.getKotlinInvocationParameterTypes
import dev.freya02.botcommands.jda.ktx.utils.toFullyQualifiedSignature
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassRefTypeSignature
import io.github.classgraph.MethodInfo
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction
import net.dv8tion.jda.api.utils.concurrent.Task
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.jvm.kotlinFunction
import kotlin.test.Test

class RetrieveOrNullExtensionsPresenceTest {

    private val blacklist: Set<String> = setOf(
        // Functions that can't have missing entities
        "net.dv8tion.jda.api.JDA.retrieveApplicationInfo()",
        "net.dv8tion.jda.api.entities.Guild.retrieveCommandPrivileges()",
        "net.dv8tion.jda.api.entities.Guild.retrieveMetaData()",
        "net.dv8tion.jda.api.entities.Guild.retrievePrunableMemberCount(int)",
        "net.dv8tion.jda.api.entities.User.retrieveProfile()",
        "net.dv8tion.jda.api.entities.channel.concrete.GroupChannel.retrieveOwner()",
        "net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel.retrieveUser()",
        "net.dv8tion.jda.api.entities.emoji.RichCustomEmoji.retrieveOwner()",
        "net.dv8tion.jda.api.entities.sticker.GuildSticker.retrieveOwner()",
        "net.dv8tion.jda.api.events.guild.scheduledevent.GenericScheduledEventUserEvent.retrieveUser()",
        "net.dv8tion.jda.api.events.guild.scheduledevent.GenericScheduledEventUserEvent.retrieveMember()",
        "net.dv8tion.jda.api.events.message.poll.GenericMessagePollVoteEvent.retrieveUser()",
        "net.dv8tion.jda.api.events.message.poll.GenericMessagePollVoteEvent.retrieveMember()",
        "net.dv8tion.jda.api.events.message.poll.GenericMessagePollVoteEvent.retrieveMessage()",
        "net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent.retrieveUser()",
        "net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent.retrieveMember()",
        "net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent.retrieveMessage()",
        "net.dv8tion.jda.api.sharding.ShardManager.retrieveApplicationInfo()",
        "net.dv8tion.jda.api.entities.Guild.retrieveRoleMemberCounts()",
        // Pointless
        "net.dv8tion.jda.api.JDA.retrieveCommandById(java.lang.String)",
        "net.dv8tion.jda.api.JDA.retrieveCommandById(long)",
        "net.dv8tion.jda.api.JDA.retrieveApplicationEmojiById(long)",
        "net.dv8tion.jda.api.JDA.retrieveApplicationEmojiById(java.lang.String)",
        "net.dv8tion.jda.api.entities.Guild.retrieveCommandById(java.lang.String)",
        "net.dv8tion.jda.api.entities.Guild.retrieveCommandById(long)",
        // Implemented with UserSnowflake,
        "net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel.retrieveThreadMember(net.dv8tion.jda.api.entities.Member)",
        "net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel.retrieveThreadMember(net.dv8tion.jda.api.entities.User)",
    )

    @Test
    fun `All JDA retrieval methods have OrNull extensions`() {
        withRetrieveOrNullFunctions { retrieveOrNullFunctions ->
            withRetrieveMethods { retrieveMethods ->
                val missingOrNullEquivalents = retrieveMethods.filter { method ->
                    retrieveOrNullFunctions.none { isOrNullEquivalent(method, it) }
                }
                assertTrue(missingOrNullEquivalents.isEmpty()) {
                    "${missingOrNullEquivalents.size} functions miss 'OrNull' equivalents:\n${missingOrNullEquivalents.joinToString("\n") { it.toFullyQualifiedSignature() }}"
                }
            }
        }
    }

    @Test
    fun `All extensions of JDA retrieval methods returning CachedRestAction have useCache parameter`() {
        withRetrieveOrNullFunctions { retrieveOrNullFunctions ->
            withRetrieveMethods { retrieveMethods ->
                val missingUseCacheParameter = arrayListOf<MethodInfo>()

                retrieveMethods.forEach { retrieveMethod ->
                    retrieveOrNullFunctions.forEach { retrieveOrNullFunction ->
                        val match = isOrNullEquivalent(retrieveMethod, retrieveOrNullFunction)
                        if (!match) return@forEach

                        if (retrieveMethod.isReturningCacheRestAction() && retrieveOrNullFunction.hasUseCacheParameter()) {
                            missingUseCacheParameter += retrieveOrNullFunction
                        }
                    }
                }

                assertTrue(missingUseCacheParameter.isEmpty()) {
                    "${missingUseCacheParameter.size} functions miss 'useCache' parameter:\n${missingUseCacheParameter.joinToString("\n") { it.toFullyQualifiedSignature() }}"
                }
            }
        }
    }

    private fun MethodInfo.isReturningCacheRestAction(): Boolean {
        val typeRef = typeSignatureOrTypeDescriptor.resultType as? ClassRefTypeSignature ?: return false
        return typeRef.fullyQualifiedClassName == CacheRestAction::class.java.name
                || typeRef.classInfo.implementsInterface(CacheRestAction::class.java)
    }

    private fun MethodInfo.hasUseCacheParameter(): Boolean {
        return loadClassAndGetMethod().kotlinFunction!!.valueParameters.none { it.name == "useCache" }
    }

    private fun withRetrieveOrNullFunctions(block: (retrieveOrNullFunctions: List<MethodInfo>) -> Unit) {
        ClassGraph()
            .acceptPackages("io.github.freya022.botcommands", "dev.freya02.botcommands")
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()
            .use { scan ->
                scan.allClasses
                    .asSequence()
                    .flatMap { it.declaredMethodInfo }
                    .filter { it.name.startsWith("retrieve") }
                    .filter { it.name.endsWith("OrNull") }
                    .toList()
                    .also(block)
            }
    }

    private fun withRetrieveMethods(block: (retrieveMethods: List<MethodInfo>) -> Unit) {
        ClassGraph()
            .acceptPackages("net.dv8tion.jda.api")
            .enableMethodInfo()
            .scan()
            .use { scan ->
                scan.allClasses
                    .asSequence()
                    .flatMap { it.declaredMethodInfo }
                    .filterNot { it.isStatic }
                    .filter { it.name.startsWith("retrieve") }
                    .filterNot { it.toFullyQualifiedSignature() in blacklist }
                    .filterNot { it.isReturningPaginationAction() }
                    .filter { it.isReturningSingularObject() }
                    .toList()
                    .also(block)
            }
    }

    private fun MethodInfo.isReturningPaginationAction(): Boolean {
        val typeRef = typeSignatureOrTypeDescriptor.resultType as? ClassRefTypeSignature ?: return false
        return typeRef.classInfo.implementsInterface(PaginationAction::class.java)
    }

    private fun MethodInfo.isReturningSingularObject(): Boolean {
        val returnType = loadClassAndGetMethod().kotlinFunction!!.returnType
        val returnTypeErasure = returnType.jvmErasure

        val resultType = when {
            returnTypeErasure == CacheRestAction::class -> returnType.findErasureOfAt<CacheRestAction<*>>(0)
            returnTypeErasure.isSubclassOf(RestAction::class) -> returnType.findErasureOfAt<RestAction<*>>(0)
            returnTypeErasure.isSubclassOf(Task::class) -> returnType.findErasureOfAt<Task<*>>(0)
            else -> error("Unhandled return type ${returnType.jvmErasure.jvmName}")
        }

        return !resultType.jvmErasure.isSubclassOf(Iterable::class)
    }

    private fun isOrNullEquivalent(javaMethod: MethodInfo, kotlinFunction: MethodInfo): Boolean {
        return javaMethod.name == kotlinFunction.name.removeSuffix("OrNull")
                && javaMethod.getJavaInvocationParameterTypes() == kotlinFunction.getKotlinInvocationParameterTypes()
    }
}
