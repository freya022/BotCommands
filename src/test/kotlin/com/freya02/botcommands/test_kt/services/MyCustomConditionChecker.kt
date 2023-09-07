package com.freya02.botcommands.test_kt.services

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.CustomConditionChecker
import com.freya02.botcommands.test_kt.services.annotations.RequiredNumber
import kotlin.random.Random

object MyCustomConditionChecker : CustomConditionChecker<RequiredNumber> {
    private val number = Random.Default.nextInt(3) // 0, 1, 2

    override val annotationType = RequiredNumber::class.java

    override fun checkServiceAvailability(
        context: BContext,
        checkedClass: Class<*>,
        annotation: RequiredNumber
    ): String? {
        if (annotation.number != number) {
            return "The service does not have the same number, expected $number, got ${annotation.number}"
        }

        return null
    }
}