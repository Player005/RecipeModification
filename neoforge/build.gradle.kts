plugins {
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.2.+"
}

// put a repositories block here for neoforge-only repositories if you need it

dependencies {
    minecraft("net.minecraftforge:forge:${rootProject.properties["minecraft_version"]}-${rootProject.properties["forge_version"]}")
    implementation(project.project(":common").sourceSets.getByName("main").output)
    annotationProcessor(implementation("io.github.llamalad7:mixinextras-common:0.3.5")!!)

    // Add neoforge-only dependencies here.
}

minecraft {
    val forgeParchmentVersion = rootProject.properties["parchment_version"].toString()
        .replace(":", "-") + "-" + rootProject.properties["minecraft_version"]
    mappings("parchment", forgeParchmentVersion)

    copyIdeResources = true

    val transformerFile = file("src/neoforge/resources/META-INF/accesstransformer.cfg")
    if (transformerFile.exists()) {
        accessTransformer(transformerFile)
    }

    runs {
        create("client") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")

            mods {
                create(rootProject.properties["mod_id"].toString()) {
                    client(true)
                    source(sourceSets.main.get())
                    source(project(":common").sourceSets.main.get())
                }
            }
        }
    }
}

tasks {
    jar {
        // add common code to jar
        val main = project.project(":common").sourceSets.main.get()
        from(main.output.classesDirs)
        from(main.output.resourcesDir)

        finalizedBy("reobfJar")
    }

    named("compileTestJava").configure {
        enabled = false
    }

    val notNeoTask: (Task) -> Boolean = { it: Task ->
        !it.name.startsWith("compileService")
    }

    withType<JavaCompile>().matching(notNeoTask).configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }

    withType<Javadoc>().matching(notNeoTask).configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }

    withType<ProcessResources>().configureEach {
        dependsOn(":common:processResources")
        from(project(":common").sourceSets.main.get().resources)

        // make all properties defined in gradle.properties usable in the mods.toml
        filesMatching("META-INF/mods.toml") {
            expand(rootProject.properties)
        }
    }
}
