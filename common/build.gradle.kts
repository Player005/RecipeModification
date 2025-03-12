@file:Suppress("UnstableApiUsage")

plugins {
    id("fabric-loom") version ("1.8.9")
}

// you can put a repositories block here if you need common dependencies from sources other than modrinth

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.properties["minecraft_version"]}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${rootProject.properties["parchment_version"]}@zip")
    })

    // mixin extras is included by default in both fabric and neoforge (no additional dependency required)
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor(implementation("io.github.llamalad7:mixinextras-common:0.3.5")!!)

    compileOnly("net.fabricmc:sponge-mixin:0.15.3+mixin.0.8.7")
    modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"]}")

    testImplementation("net.fabricmc:fabric-loader-junit:${rootProject.properties["fabric_loader_version"]}")
}

loom {
    // If you need to add access wideners, put the path here
    /** IMPORTANT: these will get added to fabric automatically, but since forge uses a different system      *
    /   (access transformers), make sure to create access transformers in the neoforge submodule if necessary */
    accessWidenerPath = file("src/main/resources/recipe_modification.accesswidener")

    mixin {
        useLegacyMixinAp = false
    }
}

tasks {
    jar { enabled = false }
    remapJar { enabled = false }

    test {
        enabled = false
        useJUnitPlatform()
    }

    processResources {
        doFirst {
            if (inputs.properties.containsKey("isRelease")) {
                exclude("*/testing/*")
            }
        }
    }
}
