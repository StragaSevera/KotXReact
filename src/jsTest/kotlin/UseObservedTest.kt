package ru.ought.kotx.react

import de.infix.testBalloon.framework.core.testSuite
import io.kotest.matchers.shouldBe
import kotlin.js.Promise
import react.FC
import react.StrictMode
import react.Suspense
import react.create
import react.dom.flushSync
import react.dom.html.ReactHTML.span
import ru.ought.kotx.reaction.InvalidationListener
import ru.ought.kotx.tracking.SubscriptionSource

@Suppress("unused")
val UseObservedTests by testSuite {
    test("writes to a read source update the committed component") {
        val source = TestSource(1)
        val Reader = FC {
            val value = useObserved { source.value }
            span { +value.toString() }
        }

        MountedRoot().use { view ->
            flushSync { view.root.render(StrictMode.create { Reader() }) }
            flushSync { source.value = 2 }
            view.host.textContent.shouldBe("2")
        }
    }

    test("a committed branch change replaces its dependencies") {
        val chooseLeft = TestSource(true)
        val left = TestSource("left")
        val right = TestSource("right")
        val Reader = FC {
            val text = useObserved { if (chooseLeft.value) left.value else right.value }
            span { +text }
        }

        MountedRoot().use { view ->
            flushSync { view.root.render(StrictMode.create { Reader() }) }
            left.subscriberCount.shouldBe(1)
            right.subscriberCount.shouldBe(0)

            flushSync { chooseLeft.value = false }

            left.subscriberCount.shouldBe(0)
            right.subscriberCount.shouldBe(1)
        }
    }

    test("unmount releases subscriptions after Strict Mode replays setup") {
        val source = TestSource(1)
        val Reader = FC { useObserved { source.value } }

        MountedRoot().use { view ->
            flushSync { view.root.render(StrictMode.create { Reader() }) }
            source.subscriberCount.shouldBe(1)
            flushSync { view.root.render(null) }
            source.subscriberCount.shouldBe(0)
        }
    }

    test("a render suspended before its first commit leaves no subscription") {
        val source = TestSource(1)
        val pending = Promise<Unit> { _, _ -> }
        val Reader = FC {
            useObserved { source.value }
            suspendRender(pending)
        }

        MountedRoot().use { view ->
            flushSync {
                view.root.render(
                    StrictMode.create {
                        Suspense {
                            fallback = span.create { +"Waiting" }
                            Reader()
                        }
                    }
                )
            }
            view.host.textContent.shouldBe("Waiting")
            source.subscriberCount.shouldBe(0)
        }
    }

    test("a write after render and before subscription reaches the component") {
        val source = TestSource(1, beforeSubscribe = { value = 2 })
        val Reader = FC {
            val value = useObserved { source.value }
            span { +value.toString() }
        }

        MountedRoot().use { view ->
            flushSync { view.root.render(StrictMode.create { Reader() }) }
            view.host.textContent.shouldBe("2")
        }
    }
}

private class TestSource<T>(
    initialValue: T,
    private val beforeSubscribe: TestSource<T>.() -> Unit = {},
) : SubscriptionSource() {
    private val subscriptions = mutableSetOf<InvalidationListener>()
    val subscriberCount: Int get() = subscriptions.size

    var value = initialValue
        get() {
            reportRead()
            return field
        }
        set(newValue) {
            if (field == newValue) return
            field = newValue
            notifySubscribers()
        }

    override fun addListener(listener: InvalidationListener) {
        beforeSubscribe()
        super.addListener(listener)
        subscriptions.add(listener)
    }

    override fun removeListener(listener: InvalidationListener) {
        super.removeListener(listener)
        subscriptions.remove(listener)
    }
}

private fun suspendRender(pending: Promise<Unit>) {
    js("throw pending")
}
