@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
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
        val vmArgs = arrayOf("-XX:+UseZGC", "-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition", "-Xms500M", "-Xmx2G")
        named("client") {
            client()
            configName = "Fabric/Client"
            vmArgs(*vmArgs)
        }
        named("server") {
            server()
            configName = "Fabric/Server"
            vmArgs(*vmArgs)
        }
    }

    // include access wideners from common
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

        // the properties listed here can be used in the fabric.mod.json
        val properties =
            listOf(
                "mc_versions_fabric", "mod_version", "mod_id", "mod_name",
                "mod_description", "mod_authors", "mod_license"
            )

        val map = mutableMapOf<String, String>()
        properties.forEach { map[it] = rootProject.properties[it].toString() }
        inputs.property("property_map", map)

        filesMatching("fabric.mod.json") {
            @Suppress("UNCHECKED_CAST")
            expand(inputs.properties["property_map"] as Map<String, String>)
        }

        doFirst {
            if (inputs.properties.containsKey("isRelease")) {
                exclude("*/testing/*")
            }
        }
    }

    named("compileTestJava").configure {
        enabled = false
    }

    named("test").configure {
        enabled = false
    }
}
