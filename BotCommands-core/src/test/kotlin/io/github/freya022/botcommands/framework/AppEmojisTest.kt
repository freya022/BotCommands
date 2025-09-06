package io.github.freya022.botcommands.framework

import io.github.classgraph.ClassGraph
import io.github.classgraph.Resource
import io.github.classgraph.ResourceList
import io.github.freya022.botcommands.api.core.config.BAppEmojisConfigBuilder
import io.github.freya022.botcommands.api.emojis.AppEmojisRegistry
import io.github.freya022.botcommands.api.emojis.annotations.AppEmojiContainer
import io.github.freya022.botcommands.api.emojis.exceptions.EmojiAlreadyExistsException
import io.github.freya022.botcommands.api.emojis.exceptions.NoEmojiResourceException
import io.github.freya022.botcommands.api.emojis.exceptions.NonUniqueEmojiResourceException
import io.github.freya022.botcommands.api.emojis.exceptions.OutOfAppEmojisException
import io.github.freya022.botcommands.internal.emojis.AppEmojiContainerData
import io.github.freya022.botcommands.internal.emojis.AppEmojiContainerProcessor
import io.github.freya022.botcommands.internal.emojis.AppEmojisLoader
import io.mockk.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.reflect.KClass
import kotlin.test.assertTrue

private const val EXAMPLE_BASE_PATH = "/my_emojis"
private const val EXAMPLE_BASE_PATH_2 = "/my_other_emojis"
private const val EXAMPLE_ASSET_PATTERN = "my_asset.**"
private const val EXAMPLE_EMOJI_NAME = "my_emoji-1"
private const val EXAMPLE_EMOJI_NAME_2 = "my_emoji-2"
private const val TEST_IDENTIFIER = "ident"

abstract class AbstractAppEmojisTest {

    @BeforeEach
    fun setup() {
        AppEmojisLoader.clear()
    }

    protected inline fun <reified T : Throwable> unwrapException(block: () -> Unit) {
        return try {
            block()
        } catch (e: Throwable) {
            if (e is T) {
                throw e.cause!!
            } else {
                throw e
            }
        }
    }
}

class AppEmojisTest : AbstractAppEmojisTest() {

    @AppEmojiContainer
    object EagerLazyMix {
        val emoji1: ApplicationEmoji by AppEmojisRegistry
        val emoji2: ApplicationEmoji by AppEmojisRegistry.lazy(EXAMPLE_ASSET_PATTERN, EXAMPLE_EMOJI_NAME)
        val emoji3: ApplicationEmoji by AppEmojisRegistry.lazy(::emoji3)
    }

    @Test
    fun `Mixing eager and lazy app emojis throws IAE`() {
        AppEmojiContainerProcessor.emojiClasses += AppEmojiContainerData(EagerLazyMix::class, EagerLazyMix::class.java.getDeclaredAnnotation(AppEmojiContainer::class.java)!!)
        val exception = assertThrows<IllegalArgumentException> {
            AppEmojisLoader(BAppEmojisConfigBuilder().build())
        }
        assertTrue(exception.message!!.startsWith("Cannot mix lazy and eager properties"))
    }

    @Test
    fun `Not loaded yet throws ISE`() {
        assertThrows<IllegalStateException> {
            unwrapException<ExceptionInInitializerError> {
                EagerLazyMix.emoji1
            }
        }
    }

    object NotEmojiContainer {
        val emoji1: ApplicationEmoji by AppEmojisRegistry
    }

    @Test
    fun `Not called by AppEmojiContainer throws ICE`() {
        assertThrows<IllegalCallerException> {
            NotEmojiContainer.emoji1
        }
    }

    @Test
    fun `Check prerequisites`() {
        assertDoesNotThrow { AppEmojisLoader.register(EXAMPLE_BASE_PATH, EXAMPLE_ASSET_PATTERN, EXAMPLE_EMOJI_NAME, TEST_IDENTIFIER) }

        assertThrows<IllegalArgumentException> { AppEmojisLoader.register(basePath = "base", EXAMPLE_ASSET_PATTERN, EXAMPLE_EMOJI_NAME, TEST_IDENTIFIER) }
        assertThrows<IllegalArgumentException> { AppEmojisLoader.register(basePath = "base/", EXAMPLE_ASSET_PATTERN, EXAMPLE_EMOJI_NAME, TEST_IDENTIFIER) }

        assertThrows<IllegalArgumentException> { AppEmojisLoader.register(EXAMPLE_BASE_PATH, EXAMPLE_ASSET_PATTERN, emojiName = "a", TEST_IDENTIFIER) }
        assertThrows<IllegalArgumentException> { AppEmojisLoader.register(EXAMPLE_BASE_PATH, EXAMPLE_ASSET_PATTERN, emojiName = "abc$", TEST_IDENTIFIER) }
    }

    @Test
    fun `Can't register same emoji name twice`() {
        assertDoesNotThrow { AppEmojisLoader.register(EXAMPLE_BASE_PATH, EXAMPLE_ASSET_PATTERN, EXAMPLE_EMOJI_NAME, TEST_IDENTIFIER) }
        assertThrows<EmojiAlreadyExistsException> { AppEmojisLoader.register(EXAMPLE_BASE_PATH, EXAMPLE_ASSET_PATTERN, EXAMPLE_EMOJI_NAME, TEST_IDENTIFIER) }
    }

    @Test
    fun `Can't register same identifier twice`() {
        assertDoesNotThrow { AppEmojisLoader.register(EXAMPLE_BASE_PATH, EXAMPLE_ASSET_PATTERN, EXAMPLE_EMOJI_NAME, TEST_IDENTIFIER) }
        assertThrows<IllegalStateException> { AppEmojisLoader.register(EXAMPLE_BASE_PATH, EXAMPLE_ASSET_PATTERN, EXAMPLE_EMOJI_NAME_2, TEST_IDENTIFIER) }
    }

    @AppEmojiContainer
    object MultipleCandidates {
        val emoji1: ApplicationEmoji by AppEmojisRegistry.lazy(::emoji1, assetPattern = "**")
    }

    @Test
    fun `Multiple resource candidates throws`() {
        val loader = createAppEmojisLoader(MultipleCandidates::class)

        withScannedResources(createDefaultResources("emojis/**", n = 2)) {
            val jda = mockk<JDA> {
                every { retrieveApplicationEmojis().complete() } returns emptyList()
            }
            assertThrows<NonUniqueEmojiResourceException> {
                loader.loadEmojis(jda)
            }
        }
    }

    @AppEmojiContainer
    object NoCandidate {
        val emoji1: ApplicationEmoji by AppEmojisRegistry.lazy(::emoji1, assetPattern = "blah")
    }

    @Test
    fun `No resource candidates throws`() {
        val loader = createAppEmojisLoader(NoCandidate::class)
        withScannedResources(createDefaultResources("emojis/blah", n = 0)) {
            val jda = mockk<JDA> {
                every { retrieveApplicationEmojis().complete() } returns emptyList()
            }
            assertThrows<NoEmojiResourceException> {
                loader.loadEmojis(jda)
            }
        }
    }

    @AppEmojiContainer
    object SingleCandidate {
        val kotlin: ApplicationEmoji by AppEmojisRegistry.lazy(::kotlin)
    }

    @Test
    fun `Single resource candidate`() {
        val loader = createAppEmojisLoader(SingleCandidate::class)
        withScannedResources(createDefaultResources("emojis/kotlin.**", n = 1)) {
            val jda = mockk<JDA> {
                every { retrieveApplicationEmojis().complete() } returns emptyList()
                every { createApplicationEmoji(any(), any()).complete() } returns mockk()
            }
            assertDoesNotThrow {
                loader.loadEmojis(jda)
            }
        }
    }

    @Test
    fun `Cannot exceed app emoji limit`() {
        mockkObject(AppEmojisLoader.Companion) {
            every { AppEmojisLoader.maxAppEmojis } returns 1

            AppEmojisLoader.register(EXAMPLE_BASE_PATH, EXAMPLE_ASSET_PATTERN, EXAMPLE_EMOJI_NAME, TEST_IDENTIFIER)
            assertThrows<OutOfAppEmojisException> {
                AppEmojisLoader.register(EXAMPLE_BASE_PATH, EXAMPLE_ASSET_PATTERN, EXAMPLE_EMOJI_NAME_2, TEST_IDENTIFIER)
            }
        }
    }

    @AppEmojiContainer
    object SingleAnnotatedCandidate {
        val kotlin: ApplicationEmoji by AppEmojisRegistry
    }

    @Test
    fun `Must delete old, unmanaged app emojis`() {
        val loader = createAppEmojisLoader(SingleAnnotatedCandidate::class, BAppEmojisConfigBuilder().apply { deleteOnOutOfSlots = true })

        val existingEmoji = mockk<ApplicationEmoji> {
            every { name } returns "existing-emoji"
            every { timeCreated } returns OffsetDateTime.of(LocalDate.now(), LocalTime.of(0, 0), ZoneOffset.UTC)
            every { delete().complete() } returns mockk()
        }
        val jda = mockk<JDA> {
            every { retrieveApplicationEmojis().complete() } returns listOf(existingEmoji)
            every { createApplicationEmoji(any(), any()).complete() } returns mockk()
        }

        mockkObject(AppEmojisLoader.Companion) {
            every { AppEmojisLoader.maxAppEmojis } returns 1

            withScannedResources(createDefaultResources("emojis/kotlin.**", n = 1)) {
                assertDoesNotThrow { loader.loadEmojis(jda) }
            }
        }

        // Make sure the emoji was attempted to be deleted
        verify(exactly = 1) { existingEmoji.delete() }
    }

    @Test
    fun `Must delete old, unmanaged app emojis, oldest first`() {
        val loader = createAppEmojisLoader(SingleAnnotatedCandidate::class, BAppEmojisConfigBuilder().apply { deleteOnOutOfSlots = true })

        val oldestEmoji = mockk<ApplicationEmoji> {
            every { name } returns "existing-emoji-2"
            every { timeCreated } returns OffsetDateTime.of(LocalDate.now(), LocalTime.of(0, 0), ZoneOffset.UTC)
            every { delete().complete() } returns mockk()
        }

        val keptEmoji = mockk<ApplicationEmoji> {
            every { name } returns "existing-emoji"
            every { timeCreated } returns oldestEmoji.timeCreated.plusHours(1)
            every { delete().complete() } answers { fail("Emoji should be kept") }
        }

        val jda = mockk<JDA> {
            // Make two emojis, the oldest one as the last in the list just to make sure
            every { retrieveApplicationEmojis().complete() } returns listOf(
                keptEmoji,
                oldestEmoji
            )
            every { createApplicationEmoji(any(), any()).complete() } returns mockk()
        }

        mockkObject(AppEmojisLoader.Companion) {
            every { AppEmojisLoader.maxAppEmojis } returns 2

            assertDoesNotThrow {
                withScannedResources(createDefaultResources("emojis/kotlin.**", n = 1)) {
                    loader.loadEmojis(jda)
                }
            }
        }

        // Make sure only the oldest one is deleted
        verify(exactly = 0) { keptEmoji.delete() }
        verify(exactly = 1) { oldestEmoji.delete() }
    }

    @Test
    fun `Must delete old, unmanaged app emojis, but not configured to`() {
        val loader = spyk(createAppEmojisLoader(SingleAnnotatedCandidate::class))

        val existingEmoji = mockk<ApplicationEmoji> {
            every { name } returns "existing-emoji"
        }

        val jda = mockk<JDA> {
            every { retrieveApplicationEmojis().complete() } returns listOf(existingEmoji)
            every { createApplicationEmoji(any(), any()).complete() } returns mockk()
        }

        mockkObject(AppEmojisLoader.Companion) {
            every { AppEmojisLoader.maxAppEmojis } returns 1

            assertThrows<OutOfAppEmojisException> {
                withScannedResources(createDefaultResources("emojis/kotlin.**", n = 1)) {
                    loader.loadEmojis(jda)
                }
            }
        }

        // Make sure no emojis were attempted to be deleted
        verify(exactly = 0) { loader.getDeletableEmojis(any(), any()) }
        verify(exactly = 0) { existingEmoji.delete() }
    }

    private fun createAppEmojisLoader(emojiContainer: KClass<*>, configBuilder: BAppEmojisConfigBuilder = BAppEmojisConfigBuilder()): AppEmojisLoader {
        AppEmojiContainerProcessor.emojiClasses += AppEmojiContainerData(emojiContainer, emojiContainer.java.getDeclaredAnnotation(AppEmojiContainer::class.java)!!)
        return AppEmojisLoader(configBuilder.build())
    }

    private fun withScannedResources(vararg resources: ClassGraphResource, block: () -> Unit) {
        mockkConstructor(ClassGraph::class) {
            every { anyConstructed<ClassGraph>().scan() } returns mockk {
                every { close() } just runs

                fun createResource(configurer: Resource.() -> Unit) = mockk<Resource> {
                    every { pathRelativeToClasspathElement } returns "pathRelativeToClasspathElement"
                    configurer()
                }

                resources.forEach { (wildcardStr, resourceConfigurers) ->
                    every { getResourcesMatchingWildcard(wildcardStr) } returns ResourceList(resourceConfigurers.map(::createResource))
                }
            }

            block()
        }
    }

    private fun createDefaultResources(wildcard: String, n: Int): ClassGraphResource {
        val configurers: Array<Resource.() -> Unit> = Array(n) {
            {
                every { open() } returns ByteArrayInputStream(byteArrayOf())
            }
        }

        return createResources(wildcard, *configurers)
    }

    private fun createResources(wildcard: String, vararg resourceConfigurers: Resource.() -> Unit): ClassGraphResource {
        return ClassGraphResource(wildcard, resourceConfigurers.asList())
    }

    private data class ClassGraphResource(val wildcard: String, val resourceConfigurers: List<Resource.() -> Unit>)
}

class AppEmojiRegistrationValuesTest : AbstractAppEmojisTest() {

    @AppEmojiContainer(EXAMPLE_BASE_PATH)
    object AnnotationBasePath {
        val emoji1: ApplicationEmoji by AppEmojisRegistry.lazy(::emoji1)
    }

    @AppEmojiContainer(EXAMPLE_BASE_PATH)
    object CustomBasePath {
        val emoji1: ApplicationEmoji by AppEmojisRegistry.lazy(::emoji1, basePath = EXAMPLE_BASE_PATH_2)
    }

    @MethodSource("Base paths")
    @ParameterizedTest
    fun `Base path optional override`(initializer: () -> Unit, expectedBasePath: String) {
        mockkObject(AppEmojisLoader) {
            val basePath = slot<String>()
            every { AppEmojisLoader.register(capture(basePath), any(), any(), any()) } just runs

            // Trigger registration
            initializer()

            assertEquals(expectedBasePath, basePath.captured)
        }
    }

    @AppEmojiContainer
    object DefaultAssetPattern {
        val emoji1: ApplicationEmoji by AppEmojisRegistry.lazy(::emoji1)
    }

    @AppEmojiContainer
    object CustomAssetPattern {
        val emoji1: ApplicationEmoji by AppEmojisRegistry.lazy(::emoji1, assetPattern = EXAMPLE_ASSET_PATTERN)
    }

    @MethodSource("Asset patterns")
    @ParameterizedTest
    fun `Asset pattern optional override`(initializer: () -> Unit, expectedAssetPattern: String) {
        mockkObject(AppEmojisLoader) {
            val assetPattern = slot<String>()
            every { AppEmojisLoader.register(any(), capture(assetPattern), any(), any()) } just runs

            // Trigger registration
            initializer()

            assertEquals(expectedAssetPattern, assetPattern.captured)
        }
    }

    @AppEmojiContainer
    object DefaultEmojiName {
        val emoji1: ApplicationEmoji by AppEmojisRegistry.lazy(::emoji1)
    }

    @AppEmojiContainer
    object CustomEmojiName {
        val emoji1: ApplicationEmoji by AppEmojisRegistry.lazy(::emoji1, emojiName = EXAMPLE_EMOJI_NAME)
    }

    @MethodSource("Emoji names")
    @ParameterizedTest
    fun `Emoji name optional override`(initializer: () -> Unit, expectedEmojiName: String) {
        mockkObject(AppEmojisLoader) {
            val emojiName = slot<String>()
            every { AppEmojisLoader.register(any(), any(), capture(emojiName), any()) } just runs

            // Trigger registration
            initializer()

            assertEquals(expectedEmojiName, emojiName.captured)
        }
    }

    companion object {
        @JvmStatic
        fun `Base paths`(): List<Arguments> = listOf(
            Arguments.of({ AnnotationBasePath }, EXAMPLE_BASE_PATH),
            Arguments.of({ CustomBasePath }, EXAMPLE_BASE_PATH_2),
        )

        @JvmStatic
        fun `Asset patterns`(): List<Arguments> = listOf(
            Arguments.of({ DefaultAssetPattern }, "emoji1.**"),
            Arguments.of({ CustomAssetPattern }, EXAMPLE_ASSET_PATTERN),
        )

        @JvmStatic
        fun `Emoji names`(): List<Arguments> = listOf(
            Arguments.of({ DefaultEmojiName }, "emoji1"),
            Arguments.of({ CustomEmojiName }, EXAMPLE_EMOJI_NAME),
        )
    }
}
