plugins {
    // Applies the correct Loom variant based on the Minecraft version
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

version = "${sc.properties["mod.version"]}+${sc.current.version}"
base.archivesName = "invfull-durability-alerts"

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

val mcReleases: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://maven.shedaniel.me/", "Shedaniel", "me.shedaniel.cloth", "me.shedaniel")
    strictMaven("https://maven.terraformersmc.com/releases/", "TerraformersMC", "com.terraformersmc")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    // `mod*` configurations everywhere: loom-back-compat converts them on 26.1+
    modImplementation("net.fabricmc:fabric-loader:${sc.properties["deps.fabric_loader"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties["deps.fabric_api"]}")

    // Optional: in-game config screen via Mod Menu + Cloth Config
    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:${sc.properties["deps.cloth"]}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modCompileOnly("com.terraformersmc:modmenu:${sc.properties["deps.modmenu"]}") { isTransitive = false }
    modLocalRuntime("me.shedaniel.cloth:cloth-config-fabric:${sc.properties["deps.cloth"]}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modLocalRuntime("com.terraformersmc:modmenu:${sc.properties["deps.modmenu"]}") { isTransitive = false }
}

// Client gametest (visual smoke test) — the API exists from 1.21.4
if (sc.current.parsed >= "1.21.4") {
    fabricApi {
        configureTests {
            createSourceSet = true
            modId = "invfull-gametest"
            enableGameTests = false
            enableClientGameTests = true
            eula = true
        }
    }
}

loom {
    runConfigs.all {
        preferGradleTask = true
        runDirectory = file("run") // per version: versions/<mc>/run
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks {
    processResources {
        val props = mapOf(
            "version" to project.version.toString(),
            "minecraft" to sc.properties["mod.mc_compat"],
            "java" to requiredJava.majorVersion,
        )
        props.forEach { (k, v) -> inputs.property(k, v) }
        filesMatching("fabric.mod.json") { expand(props) }
    }

    withType<Jar> {
        from(rootProject.file("LICENSE")) { rename { "${it}_invfull" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jar and copies it to build/libs/{mod version}/"
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/${sc.properties["mod.version"]}"))
    }
}

// ---------------------------------------------------------------------------
// Publishing to Modrinth / CurseForge (per Minecraft version).
//   ./gradlew publishMods             -> dry run only (files go to build/publishMods)
//   ./gradlew publishMods -Prelease   -> real upload
// Tokens come from environment variables only: MODRINTH_TOKEN, CURSEFORGE_TOKEN.
// Project ids live in gradle.properties; an empty id skips that platform.
// ---------------------------------------------------------------------------
fun changelogFor(ver: String): String {
    val lines = rootProject.file("CHANGELOG.md").readLines(Charsets.UTF_8)
    val start = lines.indexOfFirst { it.trim() == "## $ver" }
    if (start < 0) return "See CHANGELOG.md"
    val rest = lines.drop(start + 1)
    val end = rest.indexOfFirst { it.startsWith("## ") }.let { if (it < 0) rest.size else it }
    return rest.take(end).joinToString("\n").trim()
}

publishMods {
    file = loomx.modJar.flatMap { it.archiveFile }
    displayName = "InvFull & Durability Alerts ${sc.properties["mod.version"]} for ${mcReleases.first()}${if (mcReleases.size > 1) "–" + mcReleases.last() else ""}"
    version = project.version.toString()
    changelog = changelogFor(sc.properties["mod.version"])
    type = STABLE
    modLoaders.add("fabric")
    dryRun = !providers.gradleProperty("release").isPresent

    val mrId = providers.gradleProperty("modrinth_id").getOrElse("")
    if (mrId.isNotEmpty()) modrinth {
        projectId = mrId
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.addAll(mcReleases)
        environment = CLIENT_ONLY
        requires("fabric-api")
        optional("modmenu")
        optional("cloth-config")
    }

    val cfId = providers.gradleProperty("curseforge_id").getOrElse("")
    if (cfId.isNotEmpty()) curseforge {
        projectId = cfId
        projectSlug = "invfull-durability-alerts"
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        minecraftVersions.addAll(mcReleases)
        javaVersions.add(requiredJava)
        client = true
        server = false
        requires("fabric-api")
        optional("modmenu")
        optional("cloth-config")
    }
}
