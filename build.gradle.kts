plugins {
    id("com.android.application") version "8.7.2" apply false
    id("com.android.library") version "8.7.2" apply false
    kotlin("android") version "2.1.0" apply false
    kotlin("plugin.serialization") version "2.1.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
