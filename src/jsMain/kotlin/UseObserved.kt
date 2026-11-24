package ru.ought.kotx.react

import js.coroutines.awaitCancellation
import react.useLayoutEffect
import react.useLayoutEffectOnce
import react.useState

/**
 * Tracks synchronous KotX reads in a component's render block. Call this hook
 * unconditionally from the component.
 * When the invalidator subscribes to a new source, it requests a follow-up render to check for missed writes.
 */
fun <T> useObserved(component: () -> T): T {
    // React owns the render schedule. An invalidation only advances this component's revision.
    val (_, setRevision) = useState(0)
    val (invalidator) = useState {
        ComponentInvalidator { setRevision { it + 1 } }
    }
    // Give each render attempt its own collector so React can discard the reads it collected if React abandons the render.
    val collector = DependencyCollector()
    val renderResult = collector.collectAndRender(component)

    // React cancels the coroutine when it cleans up this layout effect,
    // including on unmount and during development Strict Mode replay.
    useLayoutEffectOnce {
        awaitCancellation { invalidator.close() }
    }
    // When render is committed, update subscriptions from the component reads tracked by collector.
    // Discarded render attempts never run this effect.
    useLayoutEffect {
        invalidator.updateDependencies(collector.sources)
    }

    return renderResult
}
