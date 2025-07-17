@file:Suppress("FunctionName")

package dev.freya02.botcommands.jda.ktx.messages

import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.dv8tion.jda.api.utils.messages.MessageRequest
import java.time.temporal.TemporalAccessor

@DslMarker
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
internal annotation class MessageBuilderDSL

typealias InlineMessageCreate = InlineMessage<MessageCreateData>
typealias InlineMessageEdit = InlineMessage<MessageEditData>

inline fun MessageCreateBuilder(
    // TODO useComponentsV2
    tts: Boolean = false,
    mentions: Mentions = Mentions.default(),
    builder: InlineMessageCreate.() -> Unit = {},
): InlineMessageCreate = MessageCreateBuilder().run {
    setTTS(tts)
    mentions.applyOn(this)

    InlineMessage(this).apply(builder)
}

inline fun MessageCreate(
    // TODO useComponentsV2
    tts: Boolean = false,
    mentions: Mentions = Mentions.default(),
    builder: InlineMessageCreate.() -> Unit = {},
): MessageCreateData = MessageCreateBuilder(
    tts,
    mentions,
    builder
).build()

/**
 * Creates a [InlineMessageEdit] configured with the provided parameters and [builder] block.
 *
 * You can also retrieve the underlying builder from [InlineMessage.builder].
 *
 * @param content    Content to override existing content, empty string to remove the content
 * @param embeds     Embeds to override existing embeds, `emptyList()` to remove all
 * @param components Components to override existing components, `emptyList()` to remove all
 * @param files      Files to override existing files, `emptyList()` to remove all
 * @param mentions   The new mention filters
 * @param replace    `true` to replace the entire message, `false` to only replace specified parts
 * @param builder    Additional configuration, can override the previously provided parameters
 */
inline fun MessageEditBuilder(
    content: String? = null,
    embeds: Collection<MessageEmbed>? = null,
    components: Collection<LayoutComponent>? = null,
    files: Collection<AttachedFile>? = null,
    mentions: Mentions? = null,
    replace: Boolean = false,
    builder: InlineMessageEdit.() -> Unit = {},
): InlineMessageEdit = MessageEditBuilder().run {
    if (content != null) setContent(content)
    if (embeds != null) setEmbeds(embeds)
    if (components != null) setComponents(components)
    if (files != null) setAttachments(files)
    mentions?.applyOn(this)
    isReplace = replace
    InlineMessage(this).apply(builder)
}

/**
 * Creates a [MessageEditData] configured with the provided parameters and [builder] block.
 *
 * @param content    Content to override existing content, empty string to remove the content
 * @param embeds     Embeds to override existing embeds, `emptyList()` to remove all
 * @param components Components to override existing components, `emptyList()` to remove all
 * @param files      Files to override existing files, `emptyList()` to remove all
 * @param mentions   The new mention filters
 * @param replace    `true` to replace the entire message, `false` to only replace specified parts
 * @param builder    Additional configuration, can override the previously provided parameters
 */
inline fun MessageEdit(
    content: String? = null,
    embeds: Collection<MessageEmbed>? = null,
    components: Collection<LayoutComponent>? = null,
    files: Collection<AttachedFile>? = null,
    mentions: Mentions? = null,
    replace: Boolean = false,
    builder: InlineMessageEdit.() -> Unit = {},
): MessageEditData = MessageEditBuilder(
    content,
    embeds,
    components,
    files,
    mentions,
    replace,
    builder
).build()

inline fun Embed(
    color: Int? = null,
    timestamp: TemporalAccessor? = null,
    builder: InlineEmbed.() -> Unit = {},
): MessageEmbed {
    return EmbedBuilder(color, timestamp, builder).build()
}

inline fun EmbedBuilder(
    color: Int? = null,
    timestamp: TemporalAccessor? = null,
    builder: InlineEmbed.() -> Unit = {},
): InlineEmbed = EmbedBuilder().run {
    setTimestamp(timestamp)
    color?.let(this::setColor)
    InlineEmbed(this).apply(builder)
}

@MessageBuilderDSL
class InlineMessage<T>(val builder: AbstractMessageBuilder<T, *>) {

    var content: String? = null
        set(value) {
            builder.setContent(value)
            field = value
        }

    val files = Accumulator<AttachedFile>()

    val embeds = Accumulator<MessageEmbed>()

    inline fun embed(embed: MessageEmbed? = null, builder: InlineEmbed.() -> Unit) {
        embeds += InlineEmbed(EmbedBuilder(embed)).apply(builder).build()
    }

    val components = Accumulator<LayoutComponent>() // TODO TopLevelMessageComponent

    // TODO component functions

    var allowedMentionTypes: Set<MentionType> = MessageRequest.getDefaultMentions()
        set(value) {
            builder.setAllowedMentions(value)
            field = value
        }

    inline fun mentions(block: InlineMentions.() -> Unit) {
        val mentions = InlineMentions().apply(block)
        mentions.users.forEach { builder.mentionUsers(it) }
        mentions.roles.forEach { builder.mentionRoles(it) }
    }

    fun build(): T {
        if (files.hasItems) {
            if (builder is MessageEditBuilder) {
                builder.setAttachments(files.items)
            } else {
                builder.setFiles(files.items.filterIsInstance<FileUpload>())
            }
        }

        if (embeds.hasItems) {
            builder.setEmbeds(embeds.items)
        }

        if (components.hasItems) {
            builder.setComponents(components.items)
        }

        return builder.build()
    }

    @MessageBuilderDSL
    class InlineMentions {
        val users = mutableListOf<Long>()
        val roles = mutableListOf<Long>()

        fun user(user: UserSnowflake) {
            users.add(user.idLong)
        }
        fun user(id: String) {
            users.add(id.toLong())
        }
        fun user(id: Long) {
            users.add(id)
        }

        fun role(role: Role) {
            roles += role.idLong
        }
        fun role(id: String) {
            roles.add(id.toLong())
        }
        fun role(id: Long) {
            roles.add(id)
        }
    }
}

@MessageBuilderDSL
class InlineEmbed(val builder: EmbedBuilder) {

    constructor(embed: MessageEmbed) : this(EmbedBuilder(embed))

    var description: String? = null
        set(value) {
            builder.setDescription(value)
            field = value
        }

    var title: String? = null
        set(value) {
            builder.setTitle(value, url)
            field = value
        }

    var url: String? = null
        set(value) {
            builder.setTitle(title, value)
            field = value
        }

    var color: Int? = null
        set(value) {
            builder.setColor(value ?: Role.DEFAULT_COLOR_RAW)
            field = value
        }

    var timestamp: TemporalAccessor? = null
        set(value) {
            builder.setTimestamp(value)
            field = value
        }

    var image: String? = null
        set(value) {
            builder.setImage(value)
            field = value
        }

    var thumbnail: String? = null
        set(value) {
            builder.setThumbnail(value)
            field = value
        }

    /**
     * Sets the footer, if [name] is not set
     */
    inline fun footer(
        name: String?,
        iconUrl: String? = null,
        build: InlineFooter.() -> Unit = {},
    ) {
        val footer = InlineFooter(name, iconUrl).apply(build)
        this.builder.setFooter(footer.name, footer.iconUrl)
    }

    inline fun author(
        name: String?,
        url: String? = null,
        iconUrl: String? = null,
        build: InlineAuthor.() -> Unit = {},
    ) {
        val author = InlineAuthor(name, url, iconUrl).apply(build)
        builder.setAuthor(author.name, author.url, author.iconUrl)
    }

    inline fun field(
        name: String = EmbedBuilder.ZERO_WIDTH_SPACE,
        value: String = EmbedBuilder.ZERO_WIDTH_SPACE,
        inline: Boolean = true,
        build: InlineField.() -> Unit = {},
    ) {
        val field = InlineField(name, value, inline).apply(build)
        builder.addField(field.name, field.value, field.inline)
    }

    fun build(): MessageEmbed = builder.build()

    @MessageBuilderDSL
    class InlineFooter @PublishedApi internal constructor(
        var name: String?,
        var iconUrl: String?,
    )

    @MessageBuilderDSL
    class InlineAuthor @PublishedApi internal constructor(
        var name: String?,
        var url: String?,
        var iconUrl: String?,
    )

    @MessageBuilderDSL
    class InlineField @PublishedApi internal constructor(
        var name: String,
        var value: String,
        var inline: Boolean,
    )
}

class Accumulator<T> internal constructor() {
    private val _items = mutableListOf<T>()
    internal val items: List<T> get() = _items
    internal var hasItems: Boolean = false
        private set

    operator fun plusAssign(items: Collection<T>) {
        hasItems = true
        _items += items
    }

    operator fun plusAssign(item: T) {
        hasItems = true
        _items += item
    }

    operator fun minusAssign(items: Collection<T>) {
        hasItems = true
        _items -= items
    }

    operator fun minusAssign(item: T) {
        hasItems = true
        _items -= item
    }
}

sealed interface MentionConfig {
    val type: MentionType

    companion object {
        val ALL_USERS: MentionConfig = MassMentionConfig(MentionType.USER)
        val ALL_ROLES: MentionConfig = MassMentionConfig(MentionType.ROLE)
        val EVERYONE: MentionConfig = MassMentionConfig(MentionType.EVERYONE)
        val HERE: MentionConfig = MassMentionConfig(MentionType.HERE)

        fun users(list: Collection<Long>): MentionConfig = WhitelistMentionConfig(MentionType.USER, list.toList())
        fun roles(list: Collection<Long>): MentionConfig = WhitelistMentionConfig(MentionType.ROLE, list.toList())

        fun disabled(type: MentionType): MentionConfig = DisabledMentionConfig(type)
        fun mass(type: MentionType, enabled: Boolean = true): MentionConfig {
            return if (enabled) {
                MassMentionConfig(type)
            } else {
                disabled(type)
            }
        }
        fun whitelist(type: MentionType, list: Collection<Long>): MentionConfig = WhitelistMentionConfig(type, list.toList())
    }
}

class DisabledMentionConfig(override val type: MentionType) : MentionConfig

class MassMentionConfig(override val type: MentionType) : MentionConfig

class WhitelistMentionConfig(
    override val type: MentionType,
    val list: List<Long>,
) : MentionConfig {
    init {
        require(type in allowedTypes) {
            "MentionType.$type cannot be whitelisted!"
        }
    }

    companion object {

        private val allowedTypes = enumSetOf(MentionType.USER, MentionType.ROLE)
    }
}

data class Mentions(
    var users: MentionConfig,
    var roles: MentionConfig,
    var everyone: Boolean,
    var here: Boolean
) {
    fun applyOn(request: MessageRequest<*>) {
        val types = enumSetOf<MentionType>()
        if (everyone) types.add(MentionType.EVERYONE)
        if (here) types.add(MentionType.HERE)
        if (users is MassMentionConfig) types.add(MentionType.USER)
        if (roles is MassMentionConfig) types.add(MentionType.ROLE)

        request.setAllowedMentions(types)
        (users as? WhitelistMentionConfig)?.list?.forEach(request::mentionUsers)
        (roles as? WhitelistMentionConfig)?.list?.forEach(request::mentionRoles)
    }

    operator fun plusAssign(config: MentionConfig) {
        when (config.type) {
            MentionType.EVERYONE -> everyone = config is DisabledMentionConfig
            MentionType.HERE -> here = config is DisabledMentionConfig
            MentionType.USER -> users = config
            MentionType.ROLE -> roles = config
            else -> {}
        }
    }

    companion object {
        fun default(): Mentions {
            val defaultTypes = MessageRequest.getDefaultMentions()

            return Mentions(
                MentionConfig.mass(MentionType.USER, enabled = MentionType.USER in defaultTypes),
                MentionConfig.mass(MentionType.ROLE, enabled = MentionType.ROLE in defaultTypes),
                MentionType.EVERYONE in defaultTypes,
                MentionType.HERE in defaultTypes
            )
        }

        fun none(): Mentions {
            return Mentions(
                users = MentionConfig.disabled(MentionType.USER),
                roles = MentionConfig.disabled(MentionType.ROLE),
                everyone = false,
                here = false,
            )
        }

        fun of(vararg configs: MentionConfig): Mentions {
            val allowedMentions = default()

            for (config in configs)
                allowedMentions += config

            return allowedMentions
        }
    }
}
