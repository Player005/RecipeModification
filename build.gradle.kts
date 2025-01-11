plugins {
    id("java-library")
    id("xyz.wagyourtail.unimined") version "1.3.12"
}

repositories {
    mavenCentral()
    unimined.wagYourMaven("releases")
    unimined.fabricMaven()
    unimined.neoForgedMaven()
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.1")

    compileOnly("net.fabricmc:sponge-mixin:0.15.5+mixin.0.8.7")
    implementation(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)
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

unimined.minecraft {
    val minecraftVersion: String by properties
    val parchmentVersion: String by properties

    version(minecraftVersion)
    side("combined")

    accessWidener {
        accessWidener("src/main/resources/recipe_modification.accesswidener")
    }

    mappings {
        mojmap(minecraftVersion)
        //parchment(minecraftVersion, parchmentVersion)
        devFallbackNamespace("official")
    }

}

unimined.minecraft(sourceSets.create("fabric")) {
    val fabricVersion: String by properties

    combineWith(sourceSets.main.get())
    fabric {
        loader(fabricVersion)
        accessWidener("src/main/resources/recipe_modification.accesswidener")
    }
    defaultRemapJar = true
}

unimined.minecraft(sourceSets.create("neoforge")) {
    val neoforgeVersion: String by properties

    combineWith(sourceSets.main.get())
    neoForge {
        loader(neoforgeVersion)
        accessTransformer(aw2at("src/main/resources/recipe_modification.accesswidener"))
    }
    defaultRemapJar = true
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
