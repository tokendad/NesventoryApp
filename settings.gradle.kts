pluginManagement {
    repositories {
        google()
        mavenCentral()
        // Add this line to resolve the Compose plugin
        maven("https://maven.pkg.jetbrains.compose.org/public/p/compose/dev")
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Android-NesVentory"
include(":app")
