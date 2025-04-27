@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("fabric-loom") version "1.8.9"
}

// add a repositories block here for fabric-only dependencies if you need it

dependencies.project(":common")

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.properties["minecraft_version"]}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${rootProject.properties["parchment_version"]}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"]}")
    // This line can be removed if you don't need fabric api
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.properties["fabric_api_version"]}")

    implementation(project.project(":common").sourceSets.getByName("main").output)

    // Add fabric-only dependencies here.
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            runDir("../run/client/${properties["minecraft_version"]}")
            ideConfigGenerated(false)
        }
        named("server") {
            server()
            configName = "Fabric Server"
            runDir("../run/client/${properties["minecraft_version"]}")
            ideConfigGenerated(false)
        }
    }

    accessWidenerPath = project(":common").loom.accessWidenerPath
}

tasks {
    withType<JavaCompile> {
        // include common code in compiled jar
        source(project(":common").sourceSets.main.get().allSource)
    }

    // put all artifacts in the right directory
    withType<Jar> {
        destinationDirectory = rootDir.resolve("build").resolve("libs_fabric")
    }
    withType<RemapJarTask> {
        destinationDirectory = rootDir.resolve("build").resolve("libs_fabric")
    }

    // add common javadoc to jar
    javadoc { source(project(":common").sourceSets.main.get().allJava) }

    processResources {
        // add common resources to jar
        from(project(":common").sourceSets.main.get().resources)

        // make all properties defined in gradle.properties usable in the neoforge.mods.toml
        filesMatching("fabric.mod.json") {
            expand(rootProject.properties)
        }
    }

    named("compileTestJava").configure {
        enabled = false
    }

    named("test").configure {
        enabled = false
    }


    processResources {
        doFirst {
            if (inputs.properties.containsKey("isRelease")) {
                exclude("*/testing/*")
            }
        }
    }
}
