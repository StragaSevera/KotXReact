package ru.ought.kotx.react

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import ru.ought.kotx.reaction.InvalidationListener
import ru.ought.kotx.tracking.SubscriptionSource

@Suppress("unused")
val ComponentInvalidatorTests by testSuite {
    test("cleanup releases subscriptions after a partial setup failure") {
        val source = object : SubscriptionSource() {
            val hasSubscribers: Boolean get() = observed
        }
        val failingSource = object : SubscriptionSource() {
            override fun addListener(listener: InvalidationListener) {
                error("Subscription failed")
            }
        }

        ComponentInvalidator {}.use { invalidator ->
            shouldThrow<IllegalStateException> {
                invalidator.updateDependencies(listOf(source, failingSource))
            }
            source.hasSubscribers.shouldBe(true)
        }

        source.hasSubscribers.shouldBe(false)
    }
}
