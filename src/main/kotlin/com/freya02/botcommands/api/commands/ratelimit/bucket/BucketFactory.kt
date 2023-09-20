package com.freya02.botcommands.api.commands.ratelimit.bucket

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import java.time.Duration as JavaDuration

fun interface BucketFactory {
    fun createBucket(): Bucket

    companion object {
        fun ofCooldown(duration: Duration): BucketFactory =
            default(1, duration)

        @JvmStatic
        fun ofCooldown(duration: JavaDuration): BucketFactory =
            default(1, duration)

        //TODO explain bucket limit
        fun default(capacity: Long, duration: Duration): BucketFactory =
            default(capacity, duration.toJavaDuration())

        //TODO explain bucket limit
        @JvmStatic
        fun default(capacity: Long, duration: JavaDuration): BucketFactory = BucketFactory {
            Bucket.builder()
                .addLimit(Bandwidth.simple(capacity, duration))
                .build()
        }

        //TODO explain bucket limit
        fun spikeProtected(capacity: Long, duration: Duration, spikeCapacity: Long, spikeDuration: Duration): BucketFactory =
            spikeProtected(capacity, duration.toJavaDuration(), spikeCapacity, spikeDuration.toJavaDuration())

        //TODO explain bucket limit
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