package io.github.freya022.botcommands.framework

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.service.CustomConditionChecker
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceError
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.api.core.service.tryGetService
import io.github.freya022.botcommands.test.config.Environment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString

object CustomConditionTests {
    @Repeatable
    @Condition(type = MyConditionChecker::class, fail = false)
    annotation class MyCondition(val allow: Boolean)

    class MyConditionChecker : CustomConditionChecker<MyCondition> {
        override val annotationType: Class<MyCondition>
            get() = MyCondition::class.java

        override fun checkServiceAvailability(serviceContainer: ServiceContainer, checkedClass: Class<*>, annotation: MyCondition): String? {
            return if (!annotation.allow) "No" else null
        }
    }

    @BService
    @MyCondition(false)
    @MyCondition(false)
    class Class00

    @BService
    @MyCondition(true)
    @MyCondition(false)
    class Class10

    @BService
    @MyCondition(false)
    @MyCondition(true)
    class Class01

    @BService
    @MyCondition(true)
    @MyCondition(true)
    class Class11

    @JvmStatic
    @BeforeAll
    fun setup() {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        (LoggerFactory.getILoggerFactory() as LoggerContext).loggerList.forEach { it.level = Level.WARN }
    }

    @Test
    fun `Two conditions of same type but different data`() {
        val context = light {
            addClass<Class00>()
            addClass<Class10>()
            addClass<Class01>()
            addClass<Class11>()
        }

        val serviceResult1 = context.serviceContainer.tryGetService<Class00>()
        assertNotNull(serviceResult1.serviceError)
        assertEquals(ServiceError.ErrorType.FAILED_CUSTOM_CONDITION, serviceResult1.serviceError?.errorType)

        val serviceResult2 = context.serviceContainer.tryGetService<Class10>()
        assertNotNull(serviceResult2.serviceError)
        assertEquals(ServiceError.ErrorType.FAILED_CUSTOM_CONDITION, serviceResult2.serviceError?.errorType)

        val serviceResult3 = context.serviceContainer.tryGetService<Class01>()
        assertNotNull(serviceResult3.serviceError)
        assertEquals(ServiceError.ErrorType.FAILED_CUSTOM_CONDITION, serviceResult3.serviceError?.errorType)

        val serviceResult4 = context.serviceContainer.tryGetService<Class11>()
        assertNull(serviceResult4.serviceError)
    }

    private fun light(block: BConfigBuilder.() -> Unit) = BotCommands.create {
        disableExceptionsInDMs = true

        components {
            enable = false
        }

        textCommands {
            enable = false
        }

        applicationCommands {
            enable = false
        }

        modals {
            enable = false
        }

        addClass<FakeBot>()

        block()
    }
}