package io.github.freya022.botcommands.api.ratelimit.bucket

import io.github.bucket4j.BucketConfiguration
import io.github.freya022.botcommands.api.ratelimit.RateLimitingContext

/**
 * A supplier for [BucketConfiguration], called when a bucket is about to get created by a [BucketAccessor].
 *
 * @see Buckets
 */
interface BucketConfigurationSupplier {
    fun getConfiguration(context: RateLimitingContext): BucketConfiguration

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
    override fun getConfiguration(context: RateLimitingContext): BucketConfiguration = bucketConfiguration
}
