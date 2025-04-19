import org.gradle.jvm.tasks.Jar

plugins {
    id("idea")
    id("maven-publish")
    id("java-library")
    id("xyz.wagyourtail.unimined") version "1.3.12"
}

repositories {
    mavenCentral()
    unimined.wagYourMaven("releases")
    unimined.fabricMaven()
    unimined.neoForgedMaven()
    unimined.modrinthMaven()
}

sourceSets {
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

group = properties["group"].toString()
version = properties["version"].toString()
base.archivesName = properties["modid"].toString()

unimined {
    minecraft { // main sourceset
        val minecraftVersion: String by properties
        val parchmentVersion: String by properties

        version(minecraftVersion)
        mappings {
            intermediary()
            mojmap()
            parchment(version = parchmentVersion)

            devFallbackNamespace("official")
        }

        accessWidener {
            accessWidener("src/main/resources/recipe_modification.accesswidener")
        }

        if (!this.sourceSet.name.contains("fabric") && !this.sourceSet.name.contains("neoforge")) {
            runs.off = true
            defaultRemapJar = false
            defaultRemapSourcesJar = false
        }
    }

    // -- modloaders --

    minecraft(sourceSets.getByName("fabric")) {
        val fabricVersion: String by properties

        combineWith(sourceSets.main.get())
        fabric {
            loader(fabricVersion)
            accessWidener("src/main/resources/recipe_modification.accesswidener")
        }
        defaultRemapJar = true
    }

    minecraft(sourceSets.getByName("neoforge")) {
        val neoforgeVersion: String by properties

        combineWith(sourceSets.main.get())
        neoForge {
            loader(neoforgeVersion)
            accessTransformer(aw2at("src/main/resources/recipe_modification.accesswidener"))
        }
        defaultRemapJar = true
    }

    // -- tests --

    minecraft(sourceSets.test.get()) { // unit tests (using fabric)
        combineWith("fabric")
    }

    minecraft(sourceSets.getByName("gametests")) { // gametests (core)
        val minecraftVersion: String by properties
        val parchmentVersion: String by properties

        version(minecraftVersion)
        mappings {
            intermediary()
            mojmap()
            parchment(version = parchmentVersion)

            devFallbackNamespace("official")
        }
    }

    minecraft(sourceSets.getByName("gametests_neoforge")) { // gametests (neoforge implementation)
        val neoforgeVersion: String by properties

        combineWith("gametests")
        neoForge {
            loader(neoforgeVersion)
        }
    }

    minecraft(sourceSets.getByName("gametests_fabric")) { // gametests (fabric implementation)
        val fabricVersion: String by properties

        combineWith("gametests")
        fabric {
            loader(fabricVersion)
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        enabled = false
    }

    getByName<ProcessResources>("processFabricResources") {
        filesMatching("fabric.mod.json") {
            expand(project.properties)
        }
    }

    getByName<ProcessResources>("processNeoforgeResources") {
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(project.properties)
        }
    }

    getByName<Jar>("gametests_fabricJar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

val gametestsImplementation by configurations.getting
val gametests_neoforgeModImplementation by configurations.getting
val gametests_fabricModImplementation by configurations.getting

val fabricModImplementation by configurations.getting

val global by configurations.creating

sourceSets.forEach {
    it.runtimeClasspath += global
    it.compileClasspath += global
}

dependencies {
    global("org.jetbrains:annotations:26.0.2")

    global("net.fabricmc:sponge-mixin:0.15.5+mixin.0.8.7")
    global(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)

    gametestsImplementation(sourceSets.main.get().output)
    gametests_neoforgeModImplementation(tasks.getByName("remapNeoforgeJar").outputs.files)
    gametests_fabricModImplementation(tasks.getByName("fabricJar").outputs.files)

    fabricModImplementation(fabricApi.fabric("0.114.0+1.21.1"))

    testImplementation("net.fabricmc:fabric-loader-junit:${properties["fabricVersion"]}")

    // gametests_fabricModImplementation("maven.modrinth:create-fabric:7Ub71nPb")
    gametests_neoforgeModImplementation("maven.modrinth:create:1.21.1-6.0.4")
    gametests_neoforgeModImplementation(sourceSets["neoforge"].output)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks.getByName("fabricJar"))
            artifact(tasks.getByName("remapFabricJar"))
            artifact(tasks.getByName("neoforgeJar"))
            artifact(tasks.getByName("remapNeoforgeJar"))
        }
    }
}
