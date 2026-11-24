package ru.ought.kotx.react

import ru.ought.kotx.reaction.InvalidationListener
import ru.ought.kotx.tracking.Source

/** Requests a new render when a dependency from the last committed render changes. */
internal class ComponentInvalidator(private val requestRender: () -> Unit) : InvalidationListener, AutoCloseable {
    private val subscribedSources = mutableListOf<Source>()

    override fun onInvalidated() = requestRender()

    override fun close() {
        // Clearing the sources also lets Strict Mode set up the same instance again.
        subscribedSources.forEach(::unsubscribe)
        subscribedSources.clear()
    }

    fun updateDependencies(readSources: List<Source>) {
        // Stay subscribed to sources shared by both renders to avoid gaps and unnecessary refreshes.
        val removedSources = subscribedSources.filter { dependency -> readSources.none { it === dependency } }
        for (source in removedSources) {
            unsubscribe(source)
            subscribedSources.removeAll { it === source }
        }

        val addedSources = readSources.filter { read -> subscribedSources.none { it === read } }
        for (source in addedSources) {
            // Keep the source available to cleanup even if subscribe throws after registering.
            subscribedSources.add(source)
            subscribe(source)
        }

        // A write between render and subscription could have been missed. Refresh once for
        // the newly added sources. The next commit can settle if it reads the same sources.
        if (addedSources.isNotEmpty()) requestRender()
    }
}
