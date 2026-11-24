package ru.ought.kotx.react

import react.dom.client.createRoot
import web.dom.document
import web.html.HtmlTagName

internal class MountedRoot : AutoCloseable {
    val host = document.createElement(HtmlTagName.div)
    val root = createRoot(host)

    init {
        document.body.appendChild(host)
    }

    override fun close() {
        root.unmount()
        host.remove()
    }
}
