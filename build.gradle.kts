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
    create("testmod")
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
    side("combined")

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

unimined.minecraft(sourceSets.getByName("testmod")) {
    val neoforgeVersion: String by properties
    val minecraftVersion: String by properties
    val parchmentVersion: String by properties

    version(minecraftVersion)
    side("combined")

    mappings {
        intermediary()
        mojmap()
        parchment(version = parchmentVersion)

        devFallbackNamespace("official")
    }

    neoForge {
        loader(neoforgeVersion)
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

val testmodModImplementation by configurations.getting
val testmodCompileOnly by configurations.getting
val testModImplementation by configurations.getting

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

    testmodCompileOnly(sourceSets.main.get().output)
    testmodModImplementation(tasks.getByName("remapNeoforgeJar").outputs.files)

    testImplementation("net.fabricmc:fabric-loader-junit:${properties["fabricVersion"]}")
    testmodModImplementation(sourceSets.getByName("fabric").output)
}
