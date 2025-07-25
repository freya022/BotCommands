package dev.freya02.botcommands.jda.ktx.retrieve

import dev.freya02.botcommands.jda.ktx.DeprecatedInBcCore
import dev.freya02.botcommands.jda.ktx.IgnoreForMatch
import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.requests.runIgnoringResponseOrNull
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Entitlement
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.api.entities.sticker.StickerUnion
import net.dv8tion.jda.api.requests.ErrorResponse

/**
 * Retrieves a [User] with the provided ID.
 *
 * Throws the same exceptions as [JDA.retrieveUserById], minus [ErrorResponse.UNKNOWN_USER].
 *
 * @param userId   ID of the user to retrieve
 * @param useCache Whether this should rely on the cache, set to `false` to always make a request.
 *
 * @see JDA.retrieveUserById
 */
@DeprecatedInBcCore
suspend fun JDA.retrieveUserByIdOrNull(userId: String, @IgnoreForMatch useCache: Boolean = true): User? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_USER) {
        retrieveUserById(userId).useCache(useCache).await()
    }
}

/**
 * Retrieves a [User] with the provided ID.
 *
 * Throws the same exceptions as [JDA.retrieveUserById], minus [ErrorResponse.UNKNOWN_USER].
 *
 * @param userId   ID of the user to retrieve
 * @param useCache Whether this should rely on the cache, set to `false` to always make a request.
 *
 * @see JDA.retrieveUserById
 */
@DeprecatedInBcCore
suspend fun JDA.retrieveUserByIdOrNull(userId: Long, @IgnoreForMatch useCache: Boolean = true): User? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_USER) {
        retrieveUserById(userId).useCache(useCache).await()
    }
}

/**
 * Retrieves a sticker from the provided ID, see [JDA.retrieveSticker] for more details.
 *
 * Throws the same exceptions as [JDA.retrieveSticker], minus [ErrorResponse.UNKNOWN_STICKER].
 *
 * @see JDA.retrieveSticker
 */
@DeprecatedInBcCore
suspend fun JDA.retrieveStickerOrNull(sticker: StickerSnowflake): StickerUnion? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_STICKER) {
        retrieveSticker(sticker).await()
    }
}

/**
 * Retrieves an [Entitlement] from the provided ID, see [JDA.retrieveEntitlementById] for more details.
 *
 * Throws the same exceptions as [JDA.retrieveEntitlementById], minus [ErrorResponse.UNKNOWN_ENTITLEMENT].
 *
 * @see JDA.retrieveEntitlementById
 */
@DeprecatedInBcCore
suspend fun JDA.retrieveEntitlementByIdOrNull(entitlementId: String): Entitlement? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_ENTITLEMENT) {
        retrieveEntitlementById(entitlementId).await()
    }
}

/**
 * Retrieves an [Entitlement] from the provided ID, see [JDA.retrieveEntitlementById] for more details.
 *
 * Throws the same exceptions as [JDA.retrieveEntitlementById], minus [ErrorResponse.UNKNOWN_ENTITLEMENT].
 *
 * @see JDA.retrieveEntitlementById
 */
@DeprecatedInBcCore
suspend fun JDA.retrieveEntitlementByIdOrNull(entitlementId: Long): Entitlement? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_ENTITLEMENT) {
        retrieveEntitlementById(entitlementId).await()
    }
}

/**
 * Retrieves a [Webhook] from the provided ID, see [JDA.retrieveWebhookById] for more details.
 *
 * Throws the same exceptions as [JDA.retrieveWebhookById], minus [ErrorResponse.UNKNOWN_WEBHOOK].
 *
 * @see JDA.retrieveWebhookById
 */
@DeprecatedInBcCore
suspend fun JDA.retrieveWebhookByIdOrNull(webhookId: String): Webhook? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_WEBHOOK) {
        retrieveWebhookById(webhookId).await()
    }
}

/**
 * Retrieves a [Webhook] from the provided ID, see [JDA.retrieveWebhookById] for more details.
 *
 * Throws the same exceptions as [JDA.retrieveWebhookById], minus [ErrorResponse.UNKNOWN_WEBHOOK].
 *
 * @see JDA.retrieveWebhookById
 */
@DeprecatedInBcCore
suspend fun JDA.retrieveWebhookByIdOrNull(webhookId: Long): Webhook? {
    return runIgnoringResponseOrNull(ErrorResponse.UNKNOWN_WEBHOOK) {
        retrieveWebhookById(webhookId).await()
    }
}
