import net.neoforged.moddevgradle.dsl.RunModel

plugins {
    id("net.neoforged.moddev") version "2.0.91"
}

// put a repositories block here for neoforge-only repositories if you need it

dependencies {
    implementation(project.project(":common").sourceSets.getByName("main").output)

    // Add neoforge-only dependencies here.
}

neoForge {
    version = rootProject.properties["neoforge_version"].toString()

    parchment {
        minecraftVersion = rootProject.properties["parchment_version"].toString().split(":").first()
        mappingsVersion = rootProject.properties["parchment_version"].toString().split(":").last()
    }

    runs {
        val vmArgs = arrayOf("-XX:+UseZGC", "-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition", "-Xms500M", "-Xmx2G")
        create("Client") {
            client()
            gameDirectory = rootDir.resolve("run/client/${properties["minecraft_version"]}")
            jvmArguments.addAll(*vmArgs)
        }
        create("Server") {
            server()
            gameDirectory = rootDir.resolve("run/server/${properties["minecraft_version"]}")
            jvmArguments.addAll(*vmArgs)
        }
    }

    mods {
        create(rootProject.properties["mod_id"].toString()) {
            sourceSet(sourceSets.main.get())
        }
    }
}

tasks {
    jar {
        // add common code to jar
        val main = project.project(":common").sourceSets.main.get()
        from(main.output.classesDirs)
        from(main.output.resourcesDir)
    }

    named("compileTestJava").configure {
        enabled = false
    }

    // NeoGradle compiles the game, but we don't want to add our common code to the game's code
    val notNeoTask: (Task) -> Boolean = { !it.name.startsWith("neo") && !it.name.startsWith("compileService") }

    // add common code & javadoc to built jars (except for NeoGradle jars)
    withType<JavaCompile>().matching(notNeoTask).configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }
    withType<Javadoc>().matching(notNeoTask).configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }

    withType<ProcessResources>().matching(notNeoTask).configureEach {
        // include common resources
        from(project(":common").sourceSets.main.get().resources)

        // the properties listed here can be used in the mods.toml
        val properties =
            listOf(
                "mc_versions_neo", "neo_loader_version_range", "mod_version", "mod_id", "mod_name",
                "mod_description", "mod_authors", "mod_license"
            )

        // store a map of the properties so the configuration cache can be used
        val map = mutableMapOf<String, String>()
        properties.forEach { map[it] = rootProject.properties[it].toString() }
        inputs.property("property_map", map)

        filesMatching("META-INF/neoforge.mods.toml") {
            @Suppress("UNCHECKED_CAST")
            expand(inputs.properties["property_map"] as Map<String, String>)
        }

        doFirst {
            if (inputs.properties.containsKey("isRelease")) {
                exclude("*/testing/*")
            }
        }
    }
}
