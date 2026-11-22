pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.10"
    // Applies the right Loom variant: remapping Loom for obfuscated <26.1, plain Loom for 26.1+
    id("dev.kikugie.loom-back-compat") version "0.4.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        versions("1.21.1", "1.21.11", "26.1.2", "26.2", "26.3")
        vcsVersion = "26.3"
    }
}

rootProject.name = "invfull-durability-alerts"
