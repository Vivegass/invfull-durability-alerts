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

// Minecraft versions that have Sinytra Connector (Fabric mods on NeoForge)
val hasConnector = sc.current.version in setOf("1.21.1", "26.1.2", "26.2")

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

    // NeoForge listing (runs through Sinytra Connector): the same Fabric jar plus a marker file,
    // so it has its own hash and can be published as a separate version with its own dependencies.
    val neoforgeJar = register<Jar>("neoforgeJar") {
        group = "build"
        description = "Fabric jar repackaged for the NeoForge (Sinytra Connector) listing"
        enabled = hasConnector
        from(zipTree(loomx.modJar.flatMap { it.archiveFile }))
        from(rootProject.file("publish/neoforge/README-neoforge.txt")) { into("META-INF") }
        archiveBaseName = "invfull-durability-alerts"
        archiveVersion = project.version.toString()
        archiveClassifier = "neoforge"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    named("build") { dependsOn(neoforgeJar) }

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
    val mcRange = mcReleases.first() + if (mcReleases.size > 1) "–" + mcReleases.last() else ""
    file = loomx.modJar.flatMap { it.archiveFile }
    displayName = "InvFull & Durability Alerts ${sc.properties["mod.version"]} for $mcRange"
    version = project.version.toString()
    changelog = changelogFor(sc.properties["mod.version"])
    type = STABLE
    dryRun = !providers.gradleProperty("release").isPresent

    val neoFile = tasks.named<Jar>("neoforgeJar").flatMap { it.archiveFile }
    val neoName = "InvFull & Durability Alerts ${sc.properties["mod.version"]} for $mcRange (NeoForge)"
    val neoVersion = "${project.version}-neoforge"

    val mrId = providers.gradleProperty("modrinth_id").getOrElse("")
    if (mrId.isNotEmpty()) {
        val mrOptions = modrinthOptions {
            projectId = mrId
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            minecraftVersions.addAll(mcReleases)
            environment = CLIENT_ONLY
        }
        modrinth("modrinth") {
            from(mrOptions)
            modLoaders.add("fabric")
            requires("fabric-api")
            optional("modmenu")
            optional("cloth-config")
        }
        if (hasConnector) modrinth("modrinthNeoforge") {
            from(mrOptions)
            file = neoFile
            displayName = neoName
            version = neoVersion
            modLoaders.add("neoforge")
            requires("connector")
            requires("forgified-fabric-api")
        }
    }

    val cfId = providers.gradleProperty("curseforge_id").getOrElse("")
    if (cfId.isNotEmpty()) {
        val cfOptions = curseforgeOptions {
            projectId = cfId
            projectSlug = "invfull-durability-alerts"
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            minecraftVersions.addAll(mcReleases)
            javaVersions.add(requiredJava)
            client = true
            server = false
        }
        curseforge("curseforge") {
            from(cfOptions)
            modLoaders.add("fabric")
            requires("fabric-api")
            optional("modmenu")
            optional("cloth-config")
        }
        if (hasConnector) curseforge("curseforgeNeoforge") {
            from(cfOptions)
            file = neoFile
            displayName = neoName
            version = neoVersion
            modLoaders.add("neoforge")
            requires("sinytra-connector")
            requires("forgified-fabric-api")
        }
    }
}
