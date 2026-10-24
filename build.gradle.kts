@file:Suppress("UnstableApiUsage")

import dev.kikugie.loomx.LoomCompatDependencyExtension
import dev.kikugie.loomx.LoomCompatProjectExtension
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.prod.ClientProductionRunTask
import net.ornithemc.ploceus.api.PloceusGradleExtensionApi
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

plugins {
    java
    kotlin("jvm")
    id("dev.kikugie.loom-back-compat")
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT" apply false
    id("ploceus") version "1.17.4" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
    `maven-publish`
}

val stonecutter = extensions.getByName("stonecutter") as StonecutterBuildExtension
val mcVersion = stonecutter.current.version
val isOrnithe = mcVersion == "1.8.9"
val ploceus = if (isOrnithe) {
    pluginManager.apply("net.fabricmc.fabric-loom-remap")
    pluginManager.apply("ploceus")
    configurations.configureEach { exclude(group = "org.lwjgl.lwjgl") }
    extensions.getByType<PloceusGradleExtensionApi>().apply { setIntermediaryGeneration(2) }
} else null

run {
    val (version, loader) = stonecutter.current.project.split("-", limit = 2)
    stonecutter.properties.tags(version, loader)
}

fun optionalProperty(name: String): String? = findProperty(name)?.toString()?.takeIf { it.isNotBlank() }

stonecutter constants {
    put("skyboxify", optionalProperty("deps.skyboxify") != null)
}

val minecraftPredicate = property("mod.mc_compat") as String
val modId = property("mod.id") as String
val modVersion = property("mod.version") as String
val fabricLoaderVersion = property("deps.fabric_loader") as String
val fabricLanguageKotlinVersion = property("deps.fabric_language_kotlin") as String
val compatibleVersions: List<String> = stonecutter.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

group = property("mod.group") as String
version = "$modVersion+$mcVersion"
base.archivesName = modId

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }

    mavenLocal()
    mavenCentral()
    google()
    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.polyfrost.org/snapshots")
    maven("https://redirector.kotlinlang.org/maven/compose-dev")
    maven("https://jitpack.io") {
        content { includeGroupAndSubgroups("com.github") }
    }
    maven("https://maven.ornithemc.net/releases") {
        content { includeGroupAndSubgroups("net.ornithemc") }
    }
    maven("https://maven.fabricmc.net/") {
        content { includeGroupAndSubgroups("net.fabricmc") }
    }
    maven("https://central.sonatype.com/repository/maven-snapshots") {
        content { includeGroup("net.kyori") }
    }
    strictMaven("https://maven.terraformersmc.com/releases/", "TerraformersMC", "com.terraformersmc")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    strictMaven("https://maven.bawnorton.com/releases", "Bawnorton", "com.github.bawnorton.mixinsquared")
    strictMaven("https://maven.cloverclient.com/releases", "CloverClient", "pl.tomgirl")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "net.kyori" && requested.name == "adventure-platform-fabric" && requested.version == "7.0.0-SNAPSHOT") {
            useVersion("7.0.0")
        }
    }
}

val javaVersion = if (isOrnithe || stonecutter.current.parsed >= "26.1") 25 else 21

if (isOrnithe) {
    val ornitheMixins = setOf("MixinPlainTranslation.java", "MixinSoundListenerThrottle.java")
    sourceSets.main {
        java.exclude { "/perf/" in it.path && it.name !in ornitheMixins }
        kotlin.exclude { "/perf/" in it.path && it.name !in ornitheMixins }
    }
}

configure<KotlinJvmExtension> {
    jvmToolchain(javaVersion)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

if (isOrnithe) {
    sourceSets.test { java.exclude("**"); kotlin.exclude("**") }
} else if (stonecutter.current.parsed < "26.3") {
    sourceSets.test { kotlin.exclude("**/TxTextFastPathTest.kt") }
}

val loomExt = extensions.getByName<LoomGradleExtensionAPI>("loom")

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    if (isOrnithe) {
        mappings(ploceus!!.layeredMappings {
            mappings("net.ornithemc:feather-gen2:$mcVersion+build.${property("deps.feather_build")}:v2") {
                containsUnpick()
            }
        })
    } else {
        dependencies.extensions.getByType<LoomCompatDependencyExtension>().applyMojangMappings()
        modLocalRuntime("me.djtheredstoner:DevAuth-fabric:${property("deps.devauth")}")
        modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    }

    implementation("org.jetbrains.kotlin:kotlin-stdlib:${property("deps.kotlin")}")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${property("deps.mixin_extras")}")
    "com.github.bawnorton.mixinsquared:mixinsquared-fabric:${property("deps.mixin_squared")}".let {
        annotationProcessor(it)
        implementation(include(it)!!)
    }

    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    runtimeOnly("net.fabricmc:fabric-language-kotlin:$fabricLanguageKotlinVersion")

    val oneconfigVersion = property("deps.oneconfig") as String
    modImplementation("org.polyfrost.oneconfig:$mcVersion-${if (isOrnithe) "ornithe" else "fabric"}:$oneconfigVersion") {
        exclude(group = "net.fabricmc", module = "fabric-language-kotlin")
    }
    for (module in arrayOf("commands", "config", "config-impl", "events", "internal", "notifications", "ui", "utils", "hud")) {
        implementation("org.polyfrost.oneconfig:$module:$oneconfigVersion")
    }

    optionalProperty("deps.sodium")?.let {
        modCompileOnly("maven.modrinth:sodium:$it") { isTransitive = false }
        if (providers.gradleProperty("polydium.runtimeSodium").isPresent) {
            modLocalRuntime("maven.modrinth:sodium:$it") { isTransitive = false }
            project.configurations.getByName("testRuntimeClasspath").extendsFrom(project.configurations.getByName("localRuntime"))
        }
    }
    optionalProperty("deps.iris")?.let {
        modCompileOnly("maven.modrinth:iris:$it") { isTransitive = false }
    }
    optionalProperty("deps.skyboxify")?.let {
        modCompileOnly("maven.modrinth:skyboxify:$it") { isTransitive = false }
    }

    testImplementation("org.junit.jupiter:junit-jupiter:${property("deps.junit")}")
    testImplementation("net.fabricmc:fabric-loader-junit:$fabricLoaderVersion")
}

loomExt.mixin {
    defaultRefmapName.set("mixins.$modId.refmap.json")
}

val modOutputGroup = sourceSets.main.get().output
    .let { files(it.classesDirs, it.resourcesDir) }
    .joinToString(File.pathSeparator) { it.absolutePath }

loomExt.runs.configureEach {
    ideConfigGenerated(true)
    runDir(if (isOrnithe) "../../run/ornithe" else "../../run")
    vmArg("-Dfabric.classPathGroups=$modOutputGroup")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("fabric.classPathGroups", modOutputGroup)
    testLogging {
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<ProcessResources>().configureEach {
    val modName = project.property("mod.name") as String
    val oneconfigVersion = project.property("deps.oneconfig") as String
    inputs.property("modId", modId)
    inputs.property("modName", modName)
    inputs.property("modVersion", modVersion)
    inputs.property("minorMcVersion", minecraftPredicate)
    inputs.property("oneconfigVersion", oneconfigVersion)
    filesMatching("fabric.mod.json") {
        if (isOrnithe) filter { line ->
            line.takeUnless { "\"fabric-api\": \"*\"" in it }?.replace("\"java\": \">=21\"", "\"java\": \">=25\"")
        }
        expand(
            mapOf(
                "mod_id" to modId,
                "mod_name" to modName,
                "mod_version" to modVersion,
                "mod_description" to "Client performance optimizations for OneClient",
                "minor_mc_version" to minecraftPredicate,
                "oneconfig_version" to oneconfigVersion,
            ),
        )
    }
    filesMatching("mixins.*.json") {
        expand("id" to modId)
    }
}

if (!isOrnithe) tasks.register<ClientProductionRunTask>("prodClient") {
    optionalProperty("polydium.gameDir")?.let { runDir.set(file(it)) }
}

tasks.matching { it.name == "createMinecraftArtifacts" }.configureEach {
    dependsOn("stonecutterGenerate")
}

val modrinthId = listOf("oneconfig.publish.modrinth", "publish.modrinth").firstNotNullOfOrNull(::optionalProperty)
val modrinthToken = listOf("oneconfig.publish.modrinth.token", "publish.modrinth.token", "modrinth.token")
    .firstNotNullOfOrNull(::optionalProperty)

val curseforgeId = listOf("oneconfig.publish.curseforge", "publish.curseforge").firstNotNullOfOrNull(::optionalProperty)
val curseforgeToken = listOf("oneconfig.publish.curseforge.token", "publish.curseforge.token", "curseforge.token")
    .firstNotNullOfOrNull(::optionalProperty)

val changelogs = rootProject.file("CHANGELOG.md").takeIf { it.exists() }?.readText() ?: "No changelog provided."

val validateChangelog = tasks.register("validateChangelog") {
    description = "Validates that the changelog is written for the current version."
    group = "publishing"
    doLast {
        if (!changelogs.contains(modVersion)) {
            throw GradleException("Changelog for version $modVersion not found.")
        }
    }
}

tasks.publishMods.configure {
    dependsOn(validateChangelog)
}
tasks.matching { it.name == "publishModrinth" }.configureEach {
    dependsOn(validateChangelog)
}

val modJar = extensions.getByType<LoomCompatProjectExtension>().modJar

publishMods {
    file = modJar.flatMap { it.archiveFile }

    displayName = modVersion
    version = "v$modVersion"
    changelog = changelogs
    type = STABLE

    modLoaders.add(if (isOrnithe) "ornithe" else "fabric")

    dryRun = (modrinthId == null || modrinthToken == null) && (curseforgeId == null || curseforgeToken == null)

    if (modrinthId != null) {
        modrinth {
            projectId = modrinthId
            accessToken = modrinthToken.orEmpty()

            minecraftVersions.addAll(compatibleVersions.ifEmpty { listOf(mcVersion) })

            environment = CLIENT_ONLY

            requires("oneconfig")
            if (!isOrnithe) requires("fabric-api")
            requires("fabric-language-kotlin")
        }
    }

    if (curseforgeId != null) {
        curseforge {
            projectId = curseforgeId
            accessToken = curseforgeToken.orEmpty()

            minecraftVersions.addAll(compatibleVersions.ifEmpty { listOf(mcVersion) })

            client = true
            server = false

            requires("oneconfig")
            if (!isOrnithe) requires("fabric-api")
            requires("fabric-language-kotlin")
        }
    }
}

publishing {
    repositories {
        maven("https://repo.polyfrost.org/releases") {
            name = "releases"
            credentials {
                username = providers.gradleProperty("polyfrostRepoUsername").orNull
                password = providers.gradleProperty("polyfrostRepoToken").orNull
            }
            authentication { create<BasicAuthentication>("basic") }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = modId
        }
    }
}
