@file:Suppress("FunctionName")

package ru.ought.kotx.react

import react.ChildrenBuilder
import react.FC
import react.Props

/** Creates a function component that observes synchronous KotX reads throughout its render body. */
fun ObservingFC(block: ChildrenBuilder.() -> Unit): FC<Props> =
    FC { observing { block() } }

fun <P : Props> ObservingFC(block: ChildrenBuilder.(props: P) -> Unit): FC<P> =
    FC { props -> observing { block(props) } }

fun ObservingFC(displayName: String, block: ChildrenBuilder.() -> Unit): FC<Props> =
    ObservingFC(block).apply { this.displayName = displayName }

fun <P : Props> ObservingFC(displayName: String, block: ChildrenBuilder.(props: P) -> Unit): FC<P> =
    ObservingFC(block).apply { this.displayName = displayName }
