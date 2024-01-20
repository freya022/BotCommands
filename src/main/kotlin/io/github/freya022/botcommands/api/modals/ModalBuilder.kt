package io.github.freya022.botcommands.api.modals

import io.github.freya022.botcommands.api.modals.annotations.ModalData
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.internal.modals.ModalDSL
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import net.dv8tion.jda.api.interactions.modals.Modal as JDAModal

@ModalDSL
abstract class ModalBuilder protected constructor(
    customId: String,
    title: String
) : JDAModal.Builder(customId, title) {
    /**
     * Binds the action to a [@ModalHandler][ModalHandler] with its arguments.
     *
     * @param handlerName The name of the modal handler, which must be the same as your [@ModalHandler][ModalHandler]
     * @param userData    The optional user data to be passed to the modal handler via [@ModalData][ModalData]
     *
     * @return This builder for chaining convenience
     */
    abstract fun bindTo(handlerName: String, userData: List<Any?>): ModalBuilder

    /**
     * Binds the action to a [@ModalHandler][ModalHandler] with its arguments.
     *
     * @param handlerName The name of the modal handler, which must be the same as your [@ModalHandler][ModalHandler]
     * @param userData    The optional user data to be passed to the modal handler via [@ModalData][ModalData]
     *
     * @return This builder for chaining convenience
     */
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
    abstract fun bindTo(handler: Consumer<ModalInteractionEvent>): ModalBuilder

    /**
     * Binds the action to the closure.
     *
     * @param handler The modal handler to run when the modal is used
     *
     * @return This builder for chaining convenience
     */
    @JvmSynthetic
    abstract fun bindTo(handler: suspend (ModalInteractionEvent) -> Unit): ModalBuilder

    /**
     * Sets the timeout for this modal, the modal cannot be used after the timeout has passed.
     *
     * **Note:** It is extremely recommended to put a timeout on your modals,
     * as it would otherwise cause a memory leak if the user never sends the modal.
     *
     * @param timeout   The amount of time in the supplied time unit before the modal is removed
     * @param unit      The time unit of the timeout
     * @param onTimeout The function to run when the timeout has been reached
     *
     * @return This builder for chaining convenience
     */
    fun setTimeout(timeout: Long, unit: TimeUnit, onTimeout: Runnable): ModalBuilder {
        return setTimeout(Duration.of(timeout, unit.toChronoUnit()), onTimeout)
    }

    /**
     * Sets the timeout for this modal, the modal cannot be used after the timeout has passed.
     *
     * **Note:** It is extremely recommended to put a timeout on your modals,
     * as it would otherwise cause a memory leak if the user never sends the modal.
     *
     * @param timeout   The amount of time before the modal is removed
     * @param onTimeout The function to run when the timeout has been reached
     *
     * @return This builder for chaining convenience
     */
    abstract fun setTimeout(timeout: Duration, onTimeout: Runnable): ModalBuilder

    /**
     * An ID is already generated automatically, but you can set a custom ID if you wish to.
     *
     * **Tip:** A modal with the same ID as a previously sent one, will have the previously submitted values.
     */
    override fun setId(customId: String): ModalBuilder {
        super.setId(customId)
        return this
    }

    protected fun jdaBuild(): JDAModal {
        return super.build()
    }

    abstract override fun build(): Modal
}
