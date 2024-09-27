package io.github.freya022.botcommands.api.commands.ratelimit.bucket

import io.github.bucket4j.BucketConfiguration
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * A supplier for [BucketConfiguration], called when a bucket is about to get created by a [BucketAccessor].
 *
 * @see Buckets
 */
interface BucketConfigurationSupplier {
    fun getConfiguration(context: BContext, event: MessageReceivedEvent, commandInfo: TextCommandInfo): BucketConfiguration

    fun getConfiguration(context: BContext, event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo): BucketConfiguration

    fun getConfiguration(context: BContext, event: GenericComponentInteractionCreateEvent): BucketConfiguration

    companion object {
        /**
         * Creates a [BucketConfigurationSupplier] which always returns the given [bucketConfiguration].
         */
        @JvmStatic
        fun constant(bucketConfiguration: BucketConfiguration): BucketConfigurationSupplier =
            ConstantBucketConfigurationSupplier(bucketConfiguration)
    }
}

/**
 * Converts this configuration into a [BucketConfigurationSupplier].
 *
 * @see BucketConfigurationSupplier.constant
 */
fun BucketConfiguration.toSupplier(): BucketConfigurationSupplier =
    BucketConfigurationSupplier.constant(this)

private class ConstantBucketConfigurationSupplier(
    private val bucketConfiguration: BucketConfiguration
) : BucketConfigurationSupplier {
    override fun getConfiguration(context: BContext, event: MessageReceivedEvent, commandInfo: TextCommandInfo) = bucketConfiguration

    override fun getConfiguration(context: BContext, event: GenericCommandInteractionEvent, commandInfo: ApplicationCommandInfo) = bucketConfiguration

    override fun getConfiguration(context: BContext, event: GenericComponentInteractionCreateEvent) = bucketConfiguration
}