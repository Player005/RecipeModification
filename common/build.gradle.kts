@file:Suppress("UnstableApiUsage")

plugins {
    id("fabric-loom") version ("1.10-SNAPSHOT")
}

// you can put a repositories block here if you need common dependencies from other sources than modrinth

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.properties["minecraft_version"]}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${rootProject.properties["parchment_version"]}@zip")
    })

    // mixin extras is included by default in both fabric and neoforge (no additional dependency required)
    compileOnly("io.github.llamalad7:mixinextras-common:0.4.1")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")

    compileOnly("net.fabricmc:sponge-mixin:0.16.1+")
    modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"]}")

    // add your dependencies here
}

loom {
    accessWidenerPath = file("src/main/resources/recipe_modification.accesswidener")

    mixin {
        useLegacyMixinAp = false
    }
}

// don't generate jar files for the common code
tasks {
    jar { enabled = false }
    remapJar { enabled = false }
}
