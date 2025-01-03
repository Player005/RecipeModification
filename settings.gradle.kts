rootProject.name = "RecipeModification"

pluginManagement {
    repositories {
        maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")

        mavenCentral()

        gradlePluginPortal {
            content {
                // this is not required either, unless jcenter goes down again, then it might fix things
                excludeGroup("org.apache.logging.log4j")
            }
        }
    }
}
