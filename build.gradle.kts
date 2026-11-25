plugins {
    kotlin("multiplatform") version "2.4.10"
    id("de.infix.testBalloon") version "1.1.0"
}

group = "ru.ought.kotx"
version = "0.0.1"

kotlin {
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {
        jsMain.dependencies {
            implementation("ru.ought.kotx:core:0.0.1")
            api("org.jetbrains.kotlin-wrappers:kotlin-react:2026.9.0-19.2.8")
        }
        jsTest.dependencies {
            implementation("de.infix.testBalloon:testBalloon-framework-core:1.1.0")
            implementation("de.infix.testBalloon:testBalloon-integration-kotest-assertions:1.1.0")
            implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:2026.9.0-19.2.8")
        }
    }
}
