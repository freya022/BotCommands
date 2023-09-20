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
                .addLimit(Bandwidth.classic(capacity, Refill.greedy(capacity, duration)))
                .build()
        }

        //TODO spike protected bucket
    }
}