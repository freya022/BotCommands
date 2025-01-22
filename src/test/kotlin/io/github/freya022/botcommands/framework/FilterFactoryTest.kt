package io.github.freya022.botcommands.framework

import ch.qos.logback.classic.ClassicConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.FilterFactory
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.IFilterFactory
import io.github.freya022.botcommands.api.core.filters.exceptions.InvalidFilterFactoryAnnotationException
import io.github.freya022.botcommands.api.core.filters.exceptions.InvalidFilterTypeException
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.utils.AnnotationUtils
import io.github.freya022.botcommands.test.config.Environment
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import kotlin.io.path.absolutePathString
import kotlin.reflect.KFunction

object FilterFactoryTest {

    @JvmStatic
    @BeforeAll
    fun setup() {
        System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, Environment.logbackConfigPath.absolutePathString())
        (LoggerFactory.getILoggerFactory() as LoggerContext).loggerList.forEach { it.level = Level.WARN }
    }


    @FilterFactory(CorrectFilterFactoryImpl::class)
    annotation class CorrectFilterFactory(val int: Int)

    @BService
    class CorrectFilterFactoryImpl : IFilterFactory<CorrectFilterFactory> {

        override fun create(function: KFunction<*>, annotation: CorrectFilterFactory): Filter = mockk<ApplicationCommandFilter>()
    }

    @CorrectFilterFactory(42)
    fun correctFilterFactory() {}

    @Test
    fun `Correct filter factory`() {
        val context = mockk<BContext> {
            every { getService(CorrectFilterFactoryImpl::class) } returns CorrectFilterFactoryImpl()
        }

        assertDoesNotThrow {
            AnnotationUtils.getFilters(context, ::correctFilterFactory, ApplicationCommandFilter::class)
        }
    }


    @FilterFactory(IncorrectAnnotation::class)
    annotation class IncorrectFilterFactoryAnnotation

    @BService
    class IncorrectAnnotation : IFilterFactory<Command> {

        override fun create(function: KFunction<*>, annotation: Command): Filter = mockk<ApplicationCommandFilter>()
    }

    @IncorrectFilterFactoryAnnotation
    fun incorrectAnnotation() {}

    @Test
    fun `Incorrect annotation on factory`() {
        val context = mockk<BContext> {
            every { getService(IncorrectAnnotation::class) }
        }

        assertThrows<InvalidFilterFactoryAnnotationException> {
            AnnotationUtils.getFilters(context, ::incorrectAnnotation, ApplicationCommandFilter::class)
        }
    }


    @FilterFactory(IncorrectFilterType::class)
    annotation class IncorrectFilterTypeFactoryAnnotation

    @BService
    class IncorrectFilterType : IFilterFactory<IncorrectFilterTypeFactoryAnnotation> {

        override fun create(function: KFunction<*>, annotation: IncorrectFilterTypeFactoryAnnotation): Filter = mockk<Filter>()
    }

    @IncorrectFilterTypeFactoryAnnotation
    fun incorrectFilterType() {}

    @Test
    fun `Incorrect filter type returned by factory`() {
        val context = mockk<BContext> {
            every { getService(IncorrectFilterType::class) } returns IncorrectFilterType()
        }

        assertThrows<InvalidFilterTypeException> {
            AnnotationUtils.getFilters(context, ::incorrectFilterType, ApplicationCommandFilter::class)
        }
    }
}