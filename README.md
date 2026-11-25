# KotXReact

KotXReact adds component observation to Kotlin/JS React apps. When developing against local checkouts, include both builds in your application's `settings.gradle.kts` so the Kotlin/JS npm tasks can resolve them.

```kotlin
includeBuild("../KotX")
includeBuild("../KotXReact")
```

Add the library to `jsMain.dependencies`.

```kotlin
implementation("ru.ought.kotx:kotx-react:0.0.1")
```

For standalone library builds, KotXReact includes the sibling `KotX` checkout when present. Apps that use KotX state APIs also declare `ru.ought.kotx:core` as a direct dependency. See the [React To-Do example](../Examples/ReactToDo/README.md) for the full setup.

Define components that render KotX state with `ru.ought.kotx.react.ObservingFC`. Read KotX state as ordinary Kotlin properties anywhere in the render body.

```kotlin
val Title = ObservingFC {
    h1 { +store.title }
}
```

`ObservingFC` accepts typed props and an optional display name, just like `FC`. To add observation to an existing function component, import `ru.ought.kotx.react.observing` and wrap its render body in an unconditional `observing { ... }` call.

The component subscribes to the sources read synchronously by the last committed render and releases them on unmount. A newly subscribed source triggers a follow-up render to catch writes between render and subscription. Keep the observable sources stable across renders so this follow-up can settle.

Run the browser tests with `./gradlew jsBrowserTest`. Chrome is required. Set `CHROME_BIN` if Karma cannot find it.
