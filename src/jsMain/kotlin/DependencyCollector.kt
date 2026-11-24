package ru.ought.kotx.react

import ru.ought.kotx.KotXRuntime
import ru.ought.kotx.reaction.SourceReadListener
import ru.ought.kotx.tracking.Source

/** Records the distinct sources read by one component render attempt. */
internal class DependencyCollector : SourceReadListener {
    val sources: List<Source>
        field = mutableListOf<Source>()

    /** Runs the component's render block and records its synchronous KotX reads in [sources]. */
    fun <T> collectAndRender(component: () -> T): T =
        KotXRuntime.withReadListener(this, component)

    override fun onSourceRead(source: Source) {
        // Identity distinguishes sources that compare equal but send separate notifications.
        if (sources.none { it === source }) sources.add(source)
    }
}
