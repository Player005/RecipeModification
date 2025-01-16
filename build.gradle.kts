plugins {
    id("idea")
    id("java-library")
    id("xyz.wagyourtail.unimined") version "1.3.12"
}

repositories {
    mavenCentral()
    unimined.wagYourMaven("releases")
    unimined.fabricMaven()
    unimined.neoForgedMaven()
}

sourceSets {
    create("api")
    create("1.21.4")
    create("1.20.1")
    create("fabric")
    create("neoforge")
    create("testmod")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }

    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

group = properties["group"].toString()
version = properties["version"].toString()
base.archivesName = properties["modid"].toString()

unimined.minecraft(sourceSets.getByName("api")) {
    val minecraftVersion: String by properties
    val parchmentVersion: String by properties

    if (!this.sourceSet.name.startsWith("1.2")) {
        version(minecraftVersion)
        mappings {
            intermediary()
            mojmap()
            parchment(version = parchmentVersion)

            devFallbackNamespace("official")
        }
    }
    side("combined")
}

unimined.minecraft {
    combineWith(sourceSets.getByName("api"))

    accessWidener {
        accessWidener("src/main/resources/recipe_modification.accesswidener")
    }
}


unimined.minecraft(sourceSets.getByName("1.21.4")) {
    combineWith(sourceSets.main.get())
    version("1.21.4")

    mappings {
        mojmap()
        devFallbackNamespace("official")
    }
}

unimined.minecraft(sourceSets.getByName("1.20.1")) {
    combineWith(sourceSets.main.get())
    version("1.20.1")
    mappings {
        mojmap()
        devFallbackNamespace("official")
    }
}

unimined.minecraft(sourceSets.getByName("fabric")) {
    val fabricVersion: String by properties

    combineWith(sourceSets.main.get())
    fabric {
        loader(fabricVersion)
        accessWidener("src/main/resources/recipe_modification.accesswidener")
    }
    defaultRemapJar = true
}

unimined.minecraft(sourceSets.getByName("neoforge")) {
    val neoforgeVersion: String by properties

    combineWith(sourceSets.main.get())
    neoForge {
        loader(neoforgeVersion)
        accessTransformer(aw2at("src/main/resources/recipe_modification.accesswidener"))
    }
    defaultRemapJar = true
}

unimined.minecraft(sourceSets.test.get()) {
    combineWith("fabric")
}

unimined.minecraft(sourceSets.getByName("testmod")) {
    val neoforgeVersion: String by properties
    val minecraftVersion: String by properties
    val parchmentVersion: String by properties

    version(minecraftVersion)
    side("combined")

    mappings {
        intermediary()
        mojmap()
        parchment(version = parchmentVersion)

        devFallbackNamespace("official")
    }

    neoForge {
        loader(neoforgeVersion)
    }
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

val testmodModImplementation by configurations.getting
val testmodCompileOnly by configurations.getting
val testModImplementation by configurations.getting
val apiImplementation by configurations.getting

dependencies {
    apiImplementation("org.jetbrains:annotations:26.0.1")
    implementation("org.jetbrains:annotations:26.0.1")

    compileOnly("net.fabricmc:sponge-mixin:0.15.5+mixin.0.8.7")
    implementation(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)

    testmodCompileOnly(sourceSets.main.get().output)
    testmodModImplementation(tasks.getByName("remapNeoforgeJar").outputs.files)

    testImplementation("net.fabricmc:fabric-loader-junit:${properties["fabricVersion"]}")
    testmodModImplementation(sourceSets.getByName("fabric").output)
}
