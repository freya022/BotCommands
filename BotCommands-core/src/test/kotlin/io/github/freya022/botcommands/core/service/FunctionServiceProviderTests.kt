package io.github.freya022.botcommands.core.service

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.core.service.BCServiceContainerImpl
import io.github.freya022.botcommands.internal.core.service.provider.FunctionServiceProvider
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

object FunctionServiceProviderTests {

    private val serviceContainer: BCServiceContainerImpl = mockk()

    @Test
    fun `Top-level object instance function`() {
        val provider = FunctionServiceProvider(TopLevelObject::class, TopLevelObject::testInstanceProvider)
        assertNull(provider.canInstantiate(serviceContainer))
        assertNotNull(provider.createInstance(serviceContainer).instance)
    }

    @Test
    fun `Top-level object static function`() {
        val provider = FunctionServiceProvider(TopLevelObject::class, TopLevelObject::testStaticProvider)
        assertNull(provider.canInstantiate(serviceContainer))
        assertNotNull(provider.createInstance(serviceContainer).instance)
    }

    @Test
    fun `Companion object instance function`() {
        val provider = FunctionServiceProvider(TopLevelClass.Companion::class, TopLevelClass.Companion::testInstanceProvider)
        assertNull(provider.canInstantiate(serviceContainer))
        assertNotNull(provider.createInstance(serviceContainer).instance)
    }

    @Test
    fun `Companion object static function`() {
        val provider = FunctionServiceProvider(TopLevelClass.Companion::class, TopLevelClass.Companion::testStaticProvider)
        assertNull(provider.canInstantiate(serviceContainer))
        assertNotNull(provider.createInstance(serviceContainer).instance)
    }

    object TopLevelObject {
        @BService
        fun testInstanceProvider(): Any = Any()

        @BService
        @JvmStatic
        fun testStaticProvider(): Any = Any()
    }

    class TopLevelClass {
        companion object {
            @BService
            fun testInstanceProvider(): Any = Any()

            @BService
            @JvmStatic
            fun testStaticProvider(): Any = Any()
        }
    }
}