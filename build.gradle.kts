plugins {
    id("idea")
    id("java-library")
    id("xyz.wagyourtail.unimined") version "1.3.12"
}

repositories {
    mavenCentral()
    unimined.wagYourMaven("releases")
    unimined.fabricMaven()
    unimined.neoForgedMaven()
}

sourceSets {
    create("1.21.4")
    create("1.21.1")
    create("1.20.1")
    create("fabric")
    create("neoforge")
    create("gametests")
    create("gametests_neoforge")
    create("gametests_fabric")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }

    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

group = properties["group"].toString()
version = properties["version"].toString()
base.archivesName = properties["modid"].toString()

unimined.minecraft {
    val minecraftVersion: String by properties
    val parchmentVersion: String by properties

    if (!this.sourceSet.name.startsWith("1.")) {
        version(minecraftVersion)
        mappings {
            intermediary()
            mojmap()
            parchment(version = parchmentVersion)

            devFallbackNamespace("official")
        }
    }

    if (!this.sourceSet.name.contains("fabric") && !this.sourceSet.name.contains("neoforge")) {
        runs.off = true
        defaultRemapJar = false
        defaultRemapSourcesJar = false
    }
}

unimined.minecraft(sourceSets.getByName("1.21.4")) {
    version("1.21.4")
    combineWith(sourceSets.main.get())

    mappings {
        mojmap()
        devFallbackNamespace("official")
    }
}

unimined.minecraft(sourceSets.getByName("1.21.1")) {
    version("1.21.1")
    combineWith(sourceSets.main.get())

    mappings {
        mojmap()
        devFallbackNamespace("official")
    }
    accessWidener {
        accessWidener("src/1.21.1/resources/recipe_modification.accesswidener")
    }
}

unimined.minecraft(sourceSets.getByName("1.20.1")) {
    version("1.20.1")
    combineWith(sourceSets.main.get())

    mappings {
        mojmap()
        devFallbackNamespace("official")
    }
}

unimined.minecraft(sourceSets.getByName("fabric")) {
    val fabricVersion: String by properties

    combineWith("1.21.1")
    fabric {
        loader(fabricVersion)
        accessWidener("src/1.21.1/resources/recipe_modification.accesswidener")
    }
    defaultRemapJar = true
}

unimined.minecraft(sourceSets.getByName("neoforge")) {
    val neoforgeVersion: String by properties

    combineWith("1.21.1")
    neoForge {
        loader(neoforgeVersion)
        accessTransformer(aw2at("src/1.21.1/resources/recipe_modification.accesswidener"))
    }
    defaultRemapJar = true
}

unimined.minecraft(sourceSets.test.get()) {
    combineWith("fabric")
}

unimined.minecraft(sourceSets.getByName("gametests")) {
    val minecraftVersion: String by properties
    val parchmentVersion: String by properties

    version(minecraftVersion)

    mappings {
        mojmap()
        parchment(version = parchmentVersion)

        devFallbackNamespace("official")
    }
}

unimined.minecraft(sourceSets.getByName("gametests_neoforge")) {
    val neoforgeVersion: String by properties

    combineWith("gametests")
    neoForge {
        loader(neoforgeVersion)
    }
}

unimined.minecraft(sourceSets.getByName("gametests_fabric")) {
    val fabricVersion: String by properties

    combineWith("gametests")
    fabric {
        loader(fabricVersion)
    }
}

tasks.getByName<ProcessResources>("processFabricResources") {
    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

tasks.getByName<ProcessResources>("processNeoforgeResources") {
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(properties)
    }
}

val gametestsCompileOnly by configurations.getting
val gametests_neoforgeModImplementation by configurations.getting
val gametests_fabricModImplementation by configurations.getting

val fabricModImplementation by configurations.getting

val impls by configurations.creating
val global by configurations.creating

sourceSets.forEach {
    if (!it.name.startsWith("1.")) return@forEach
    it.runtimeClasspath += impls
    it.compileClasspath += impls
}

sourceSets.forEach {
    it.runtimeClasspath += global
    it.compileClasspath += global
}

dependencies {
    global("org.jetbrains:annotations:26.0.1")

    impls("net.fabricmc:sponge-mixin:0.15.5+mixin.0.8.7")
    impls(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)

    gametestsCompileOnly(sourceSets.main.get().output)
    gametests_neoforgeModImplementation(tasks.getByName("remapNeoforgeJar").outputs.files)
    gametests_fabricModImplementation(tasks.getByName("fabricJar").outputs.files)

    fabricModImplementation(fabricApi.fabric("0.114.0+1.21.1"))

    testImplementation("net.fabricmc:fabric-loader-junit:${properties["fabricVersion"]}")
}
