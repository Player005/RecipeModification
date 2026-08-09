@file:Suppress("UnstableApiUsage")

plugins {
    id("fabric-loom") version ("1.15.5")
}

// you can put a repositories block here if you need common dependencies from other sources than modrinth

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.properties["minecraft_version"]}")

    // mixin extras is included by default in both fabric and neoforge (no additional dependency required)
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")

    compileOnly("net.fabricmc:sponge-mixin:0.15.3+mixin.0.8.7")
    // add your dependencies here
}

loom {
    mixin {
        useLegacyMixinAp = false
    }
}

// don't generate jar files for the common code
tasks {
    jar { enabled = false }
}
