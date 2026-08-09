@file:Suppress("UnstableApiUsage")

plugins {
    id("fabric-loom") version "1.15.5"
}

repositories {
    maven("https://maven.shedaniel.me")
}

dependencies.project(":common")

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.properties["minecraft_version"]}")

    implementation("net.fabricmc:fabric-loader:${rootProject.properties["fabric_loader_version"]}")
    implementation("net.fabricmc.fabric-api:fabric-api:${rootProject.properties["fabric_api_version"]}")
    implementation(project.project(":common").sourceSets.getByName("main").output)
}

loom {
    runs {
        val vmArgs = arrayOf("-XX:+UseZGC", "-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition", "-Xms500M", "-Xmx2G")
        named("client") {
            client()
            configName = "Fabric/Client"
            ideConfigGenerated(false)
            runDir("../run/client/${properties["minecraft_version"]}")
            vmArgs(*vmArgs)
        }
        named("server") {
            server()
            configName = "Fabric/Server"
            ideConfigGenerated(false)
            runDir("../run/server/${properties["minecraft_version"]}")
            vmArgs(*vmArgs)
        }
    }

    mixin {
        useLegacyMixinAp = false
        defaultRefmapName = "recipe_modification.refmap.json"
    }

}

tasks {
    withType<JavaCompile> {
        // include common code in compiled jar
        source(project(":common").sourceSets.main.get().allSource)
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
