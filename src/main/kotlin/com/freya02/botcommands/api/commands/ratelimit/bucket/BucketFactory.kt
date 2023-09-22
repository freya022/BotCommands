package com.freya02.botcommands.api.commands.ratelimit.bucket

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import java.time.Duration as JavaDuration

/**
 * Creates rate limiting [token buckets][Bucket].
 */
fun interface BucketFactory {
    fun createBucket(): Bucket

    companion object {
        /**
         * Creates a [BucketFactory] with a single token, which gets regenerated after the given duration.
         */
        fun ofCooldown(duration: Duration): BucketFactory =
            default(1, duration)

        /**
         * Creates a [BucketFactory] with a single token, which gets regenerated after the given duration.
         */
        @JvmStatic
        fun ofCooldown(duration: JavaDuration): BucketFactory =
            default(1, duration)

        /**
         * Creates a [BucketFactory] with a simple [Bandwidth] of the specified capacity,
         * and a [greedy][Refill.greedy] refill.
         *
         * In other words, the tokens of the bucket will be progressively refilled over the duration,
         * such as the entire bucket would have had been refilled when the duration has elapsed.
         *
         * ### Example
         * For a bucket with 10 tokens and a duration of 10 seconds, 1 token will be added every second.
         *
         * @see Refill.greedy
         */
        fun default(capacity: Long, duration: Duration): BucketFactory =
            default(capacity, duration.toJavaDuration())

        /**
         * Creates a [BucketFactory] with a [Bandwidth] of the specified capacity,
         * and a [greedy][Refill.greedy] refill.
         *
         * In other words, the tokens of the bucket will be progressively refilled over the duration,
         * such as the entire bucket would have been refilled when the duration has elapsed.
         *
         * ### Example
         * For a bucket with 10 tokens and a duration of 10 seconds, 1 token will be added every second.
         *
         * @see Refill.greedy
         */
        @JvmStatic
        fun default(capacity: Long, duration: JavaDuration): BucketFactory = BucketFactory {
            Bucket.builder()
                .addLimit(Bandwidth.simple(capacity, duration))
                .build()
        }

        /**
         * Creates a [BucketFactory] with:
         * - a [Bandwidth] of the specified [capacity],
         *   and a [greedy][Refill.greedy] refill of the specified [duration].
         * - a [Bandwidth] of the specified [spike capacity][spikeCapacity],
         *   and an ["intervally"][Refill.intervally] refill of the specified [spike duration][spikeDuration].
         *
         * When a token is used, both bandwidths need to have a token available.
         *
         * In other words, the tokens of the greedy bucket will be progressively refilled over the duration,
         * such as the entire bucket would have been refilled when the duration has elapsed.
         * While the intervally refilled bucket will only be refilled once a token has been used,
         * and the *entire* duration has elapsed.
         *
         * This avoids users from trying to use all resources in a short time, forcing them to space out their usage.
         *
         * ### Example
         * For a bucket with six tokens and a duration of 1 hour, one token will be added every 10 minutes,
         * but if the spike bucket has 2 tokens and a duration of 10 minutes,
         * then the user will only be able to do 2 requests at once, then wait 10 minutes before doing 2 again,
         * until the `6 tokens / 10 minutes` bucket is empty.
         *
         * @param capacity      The number of tokens for the base bucket
         * @param duration      The duration during which the tokens of the base bucket are progressively refilled
         * @param spikeCapacity The number of tokens for the spike bucket, limiting burst usages
         * @param spikeDuration The duration after which the entire spike bucket will be refilled
         *
         * @throws IllegalArgumentException If [capacity] > [spikeCapacity]
         *
         * @see Refill.greedy
         * @see Refill.intervally
         */
        fun spikeProtected(capacity: Long, duration: Duration, spikeCapacity: Long, spikeDuration: Duration): BucketFactory =
            spikeProtected(capacity, duration.toJavaDuration(), spikeCapacity, spikeDuration.toJavaDuration())

        /**
         * Creates a [BucketFactory] with:
         * - a [Bandwidth] of the specified [capacity],
         *   and a [greedy][Refill.greedy] refill of the specified [duration].
         * - a [Bandwidth] of the specified [spike capacity][spikeCapacity],
         *   and an ["intervally"][Refill.intervally] refill of the specified [spike duration][spikeDuration].
         *
         * When a token is used, both bandwidths need to have a token available.
         *
         * In other words, the tokens of the greedy bucket will be progressively refilled over the duration,
         * such as the entire bucket would have been refilled when the duration has elapsed.
         * While the intervally refilled bucket will only be refilled once a token has been used,
         * and the *entire* duration has elapsed.
         *
         * This avoids users from trying to use all resources in a short time, forcing them to space out their usage.
         *
         * ### Example
         * For a bucket with six tokens and a duration of 1 hour, one token will be added every 10 minutes,
         * but if the spike bucket has 2 tokens and a duration of 10 minutes,
         * then the user will only be able to do 2 requests at once, then wait 10 minutes before doing 2 again,
         * until the `6 tokens / 10 minutes` bucket is empty.
         *
         * @param capacity      The number of tokens for the base bucket
         * @param duration      The duration during which the tokens of the base bucket are progressively refilled
         * @param spikeCapacity The number of tokens for the spike bucket, limiting burst usages
         * @param spikeDuration The duration after which the entire spike bucket will be refilled
         *
         * @throws IllegalArgumentException If [capacity] > [spikeCapacity]
         *
         * @see Refill.greedy
         * @see Refill.intervally
         */
        @JvmStatic
        fun spikeProtected(capacity: Long, duration: JavaDuration, spikeCapacity: Long, spikeDuration: JavaDuration): BucketFactory {
            require(capacity > spikeCapacity) { "Spike capacity must be lower than capacity" }

            return BucketFactory {
                Bucket.builder()
                    .addLimit(Bandwidth.simple(capacity, duration))
                    .addLimit(Bandwidth.classic(spikeCapacity, Refill.intervally(spikeCapacity, spikeDuration)))
                    .build()
            }
        }

        @JvmStatic
        fun custom(vararg limits: Bandwidth): BucketFactory {
            return custom(limits.asList())
        }

        @JvmStatic
        fun custom(limits: List<Bandwidth>): BucketFactory {
            return BucketFactory {
                Bucket.builder()
                    .apply {
                        for (limit in limits) {
                            addLimit(limit)
                        }
                    }
                    .build()
            }
        }
    }
}