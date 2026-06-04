package dev.freya02.botcommands.helpers

import io.github.freya022.botcommands.api.core.messages.exceptions.MissingMessageTemplateException
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.mockk.CapturingSlot
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import kotlin.reflect.KFunction
import kotlin.test.fail

abstract class AbstractMessagesTests : AbstractIntegrationTest() {

    protected fun checkMissingTemplates(methodCalls: Map<String, () -> Unit>, templatePathSlot: CapturingSlot<String>) {
        val methodsMissingTemplate: MutableList<String> = arrayListOf()
        methodCalls.values.forEach { methodCall ->
            templatePathSlot.clear()
            try {
                methodCall()
            } catch (_: MissingMessageTemplateException) {
                methodsMissingTemplate += templatePathSlot.captured
            }
        }

        if (methodsMissingTemplate.isNotEmpty()) {
            fail("The following template keys are missing default translations:\n" + methodsMissingTemplate.joinAsList())
        }
    }

    protected fun checkMissingTests(typeClazz: Class<*>, methodCalls: Map<String, () -> Unit>) {
        val missingTests =
            typeClazz.declaredMethods.mapTo(hashSetOf()) { it.name } - methodCalls.keys
        if (missingTests.isNotEmpty()) {
            fail("The following methods are missing tests:\n" + missingTests.joinAsList())
        }
    }

    protected fun <F : KFunction<MessageCreateData>> methodCall(
        callableRef: F,
        executor: F.() -> MessageCreateData,
    ): Pair<String, () -> Unit> {
        return callableRef.name to { val _ = executor(callableRef) }
    }
}
