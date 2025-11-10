package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory

/**
 * Exception when a message source factory annotated with [@MessageSourceFactory][MessageSourceFactory]
 * has a [bundle name][MessageSourceFactory.bundleName] which does not exist.
 */
class NoSuchBundleException(message: String) : IllegalArgumentException(message)
