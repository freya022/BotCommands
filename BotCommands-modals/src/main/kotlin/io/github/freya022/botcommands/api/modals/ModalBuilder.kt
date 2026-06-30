package io.github.freya022.botcommands.api.modals

import dev.freya02.botcommands.jda.ktx.components.InlineComponentDSL
import dev.freya02.botcommands.jda.ktx.components.InlineLabel
import dev.freya02.botcommands.jda.ktx.components.InlineTextDisplay
import dev.freya02.botcommands.jda.ktx.components.Label
import dev.freya02.botcommands.jda.ktx.components.TextDisplay
import io.github.freya022.botcommands.api.modals.Modal as BCModal
import io.github.freya022.botcommands.api.modals.annotations.ModalData
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.internal.modals.ModalDSL
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.ModalTopLevelComponent
import net.dv8tion.jda.api.components.label.Label
import net.dv8tion.jda.api.components.label.LabelChildComponent
import net.dv8tion.jda.api.components.tree.ComponentTree
import net.dv8tion.jda.api.modals.Modal
import net.dv8tion.jda.api.modals.Modal as JDAModal
import java.time.Duration as JavaDuration
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.annotation.CheckReturnValue
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

@ModalDSL
abstract class ModalBuilder protected constructor(
    customId: String,
    title: String
) : JDAModal.Builder(customId, title) {

    abstract val modals: Modals

    /**
     * Binds the action to a [@ModalHandler][ModalHandler] with its arguments.
     *
     * Each [@ModalInput][ModalInput] must match a component's custom ID,
     * alternatively, you can always retrieve input values from the event.
     *
     * @param handlerName The name of the modal handler, which must be the same as your [@ModalHandler][ModalHandler]
     * @param userData    The optional user data to be passed to the modal handler via [@ModalData][ModalData]
     *
     * @return This builder for chaining convenience
     */
    @CheckReturnValue
    abstract fun bindTo(handlerName: String, userData: List<Any?>): ModalBuilder

    /**
     * Binds the action to a [@ModalHandler][ModalHandler] with its arguments.
     *
     * Each [@ModalInput][ModalInput] must match a component's custom ID,
     * alternatively, you can always retrieve input values from the event.
     *
     * @param handlerName The name of the modal handler, which must be the same as your [@ModalHandler][ModalHandler]
     * @param userData    The optional user data to be passed to the modal handler via [@ModalData][ModalData]
     *
     * @return This builder for chaining convenience
     */
    @CheckReturnValue
    fun bindTo(handlerName: String, vararg userData: Any?): ModalBuilder {
        return bindTo(handlerName, userData.asList())
    }

    /**
     * Binds the action to the consumer.
     *
     * @param handler The modal handler to run when the modal is used
     *
     * @return This builder for chaining convenience
     */
    @CheckReturnValue
    fun bindTo(handler: Consumer<ModalEvent>): ModalBuilder {
        return bindTo { handler.accept(it) }
    }

    /**
     * Binds the action to the closure.
     *
     * @param handler The modal handler to run when the modal is used
     *
     * @return This builder for chaining convenience
     */
    @JvmSynthetic
    abstract fun bindTo(handler: suspend (ModalEvent) -> Unit): ModalBuilder

    /**
     * Sets the timeout for this modal, invalidating the modal after expiration,
     * and running the given timeout handler.
     *
     * If unset, the timeout is set to [Modals.defaultTimeout].
     *
     * @param timeout   The amount of time in the supplied time unit before the modal is removed
     * @param unit      The time unit of the timeout
     * @param onTimeout The function to run when the timeout has been reached
     *
     * @return This builder for chaining convenience
     */
    @JvmOverloads
    @CheckReturnValue
    fun timeout(timeout: Long, unit: TimeUnit, onTimeout: Runnable? = null): ModalBuilder {
        return timeout(JavaDuration.of(timeout, unit.toChronoUnit()), onTimeout)
    }

    /**
     * Sets the timeout for this modal, invalidating the modal after expiration,
     * and running the given timeout handler.
     *
     * If unset, the timeout is set to [Modals.defaultTimeout].
     *
     * @param timeout   The amount of time before the modal is removed
     * @param onTimeout The function to run when the timeout has been reached
     *
     * @return This builder for chaining convenience
     */
    @JvmOverloads
    @CheckReturnValue
    fun timeout(timeout: JavaDuration, onTimeout: Runnable? = null): ModalBuilder {
        return timeout(timeout.toKotlinDuration(), onTimeout?.let { { onTimeout.run() } })
    }

    /**
     * Sets the timeout for this modal, invalidating the modal after expiration,
     * and running the given timeout handler.
     *
     * If unset, the timeout is set to [Modals.defaultTimeout].
     *
     * @param timeout   The amount of time before the modal is removed
     * @param onTimeout The function to run when the timeout has been reached
     *
     * @return This builder for chaining convenience
     */
    @JvmSynthetic
    abstract fun timeout(timeout: Duration, onTimeout: (suspend () -> Unit)? = null): ModalBuilder

    override fun setTitle(title: String): ModalBuilder = apply { super.setTitle(title) }

    override fun addComponents(components: Collection<ModalTopLevelComponent>): ModalBuilder = apply {
        super.addComponents(components)
    }

    override fun addComponents(vararg components: ModalTopLevelComponent): ModalBuilder = apply {
        super.addComponents(*components)
    }

    override fun addComponents(tree: ComponentTree<out ModalTopLevelComponent>): ModalBuilder = apply {
        super.addComponents(tree)
    }

    @Deprecated("Cannot set an ID on modals managed by the framework", level = DeprecationLevel.ERROR)
    abstract override fun setId(customId: String): ModalBuilder

    protected fun internetSetId(customId: String) {
        super.setId(customId)
    }

    protected fun jdaBuild(): JDAModal {
        return super.build()
    }

    @CheckReturnValue
    abstract override fun build(): BCModal
}

@ModalDSL
@InlineComponentDSL
class InlineModal(val builder: ModalBuilder) {

    val components: MutableList<ModalTopLevelComponent> = arrayListOf()

    operator fun ModalTopLevelComponent.unaryPlus() {
        components += this
    }

    operator fun Collection<ModalTopLevelComponent>.unaryPlus() {
        components += this
    }

    /** Title of this Modal, see [Modal.Builder.setTitle] */
    var title: String
        get() = builder.title
        set(value) {
            builder.title = value
        }

    /**
     * Component that contains a label, an optional description,
     * and a [child component][LabelChildComponent], see [Label][net.dv8tion.jda.api.components.label.Label].
     *
     * @param label       Label of the Label, see [Label.withLabel]
     * @param uniqueId    Unique identifier of this component, see [Component.withUniqueId]
     * @param description The description of this Label, see [Label.withDescription]
     * @param child       The child contained by this Label, see [Label.withChild]
     * @param block       Lambda allowing further configuration
     */
    inline fun label(
        label: String?,
        uniqueId: Int = -1,
        description: String? = null,
        child: LabelChildComponent? = null,
        block: InlineLabel.() -> Unit = {},
    ) {
        builder.addComponents(Label(label, uniqueId, description, child, block))
    }

    /**
     * See [TextDisplay][net.dv8tion.jda.api.components.textdisplay.TextDisplay].
     *
     * This requires [Components V2][net.dv8tion.jda.api.utils.messages.MessageRequest.useComponentsV2] to be enabled.
     *
     * @param content  The content displayed by this component
     * @param uniqueId Unique identifier of this component
     * @param block    Lambda allowing further configuration
     */
    inline fun text(content: String? = null, uniqueId: Int = -1, block: InlineTextDisplay.() -> Unit = {}) {
        builder.addComponents(TextDisplay(content, uniqueId, block))
    }

    /**
     * Binds the action to a [@ModalHandler][ModalHandler] with its arguments.
     *
     * Each [@ModalInput][ModalInput] must match a component's custom ID,
     * alternatively, you can always retrieve input values from the event.
     *
     * @param handlerName The name of the modal handler, which must be the same as your [@ModalHandler][ModalHandler]
     * @param userData    The optional user data to be passed to the modal handler via [@ModalData][ModalData]
     */
    fun bindTo(handlerName: String, userData: List<Any?>) {
        builder.bindTo(handlerName, userData)
    }

    /**
     * Binds the action to a [@ModalHandler][ModalHandler] with its arguments.
     *
     * Each [@ModalInput][ModalInput] must match a component's custom ID,
     * alternatively, you can always retrieve input values from the event.
     *
     * @param handlerName The name of the modal handler, which must be the same as your [@ModalHandler][ModalHandler]
     * @param userData    The optional user data to be passed to the modal handler via [@ModalData][ModalData]
     */
    fun bindTo(handlerName: String, vararg userData: Any?) = builder.bindTo(handlerName, *userData)

    /**
     * Binds the action to the closure.
     *
     * @param handler The modal handler to run when the modal is used
     */
    fun bindTo(handler: suspend (ModalEvent) -> Unit) {
        builder.bindTo(handler)
    }

    /**
     * Sets the timeout for this modal, invalidating the modal after expiration,
     * and running the given timeout handler.
     *
     * If unset, the timeout is set to [Modals.defaultTimeout].
     *
     * @param timeout   The amount of time before the modal is removed
     * @param onTimeout The function to run when the timeout has been reached
     */
    @JvmSynthetic // Mute Java Duration test
    fun timeout(timeout: Duration, onTimeout: (suspend () -> Unit)? = null) {
        builder.timeout(timeout, onTimeout)
    }

    fun build(): BCModal {
        return builder.build()
    }
}
