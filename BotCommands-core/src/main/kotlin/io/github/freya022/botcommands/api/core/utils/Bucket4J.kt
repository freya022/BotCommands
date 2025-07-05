package io.github.freya022.botcommands.api.core.utils

import io.github.bucket4j.BandwidthBuilder
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * Configures refill that does refill of tokens in greedy manner,
 * it will try to add the tokens to bucket as soon as possible.
 *
 * @see BandwidthBuilder.BandwidthBuilderRefillStage.refillGreedy
 */
fun BandwidthBuilder.BandwidthBuilderRefillStage.refillGreedy(tokens: Long, period: Duration): BandwidthBuilder.BandwidthBuilderBuildStage =
    refillGreedy(tokens, period.toJavaDuration())

/**
 * Configures refill that does refill of tokens in intervally manner.
 *
 * @see BandwidthBuilder.BandwidthBuilderRefillStage.refillIntervally
 */
fun BandwidthBuilder.BandwidthBuilderRefillStage.refillIntervally(tokens: Long, period: Duration): BandwidthBuilder.BandwidthBuilderBuildStage =
    refillIntervally(tokens, period.toJavaDuration())

/**
 * Configures refill that does refill of tokens in intervally manner.
 * "Intervally" in opposite to "greedy" will wait until whole period will be elapsed before regenerate tokens.
 *
 * In additional to [refillIntervally] this method allows to specify the time when first refill should happen via timeOfFirstRefill.
 *
 * @see BandwidthBuilder.BandwidthBuilderRefillStage.refillIntervallyAligned
 */
fun BandwidthBuilder.BandwidthBuilderRefillStage.refillIntervallyAligned(tokens: Long, period: Duration, timeOfFirstRefill: Instant): BandwidthBuilder.BandwidthBuilderBuildStage =
    refillIntervallyAligned(tokens, period.toJavaDuration(), timeOfFirstRefill)

/**
 * @see BandwidthBuilder.BandwidthBuilderRefillStage.refillIntervallyAlignedWithAdaptiveInitialTokens
 */
fun BandwidthBuilder.BandwidthBuilderRefillStage.refillIntervallyAlignedWithAdaptiveInitialTokens(tokens: Long, period: Duration, timeOfFirstRefill: Instant): BandwidthBuilder.BandwidthBuilderBuildStage =
    refillIntervallyAlignedWithAdaptiveInitialTokens(tokens, period.toJavaDuration(), timeOfFirstRefill)