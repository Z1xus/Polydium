pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.fabricmc.net/")
        maven("https://maven.ornithemc.net/releases")
        maven("https://repo.polyfrost.org/releases")
        mavenCentral()
    }

    plugins {
        kotlin("jvm") version "2.4.10"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
    id("dev.kikugie.loom-back-compat") version "0.4.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter.create(rootProject) {
    val mcVersions = listOf("1.21.1", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11", "26.1", "26.2", "26.3")
    versions(mcVersions.associateBy { "$it-fabric" })
    version("1.8.9-ornithe", "1.8.9")
    vcsVersion = "26.3-fabric"
}

rootProject.name = "Polydium"
