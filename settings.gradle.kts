pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories { mavenCentral() }
}

rootProject.name = "kotx-react"

if (file("../KotX").isDirectory) {
    includeBuild("../KotX")
}
