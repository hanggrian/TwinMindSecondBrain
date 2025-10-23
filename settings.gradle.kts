pluginManagement.repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}
dependencyResolutionManagement.repositories {
    mavenCentral()
    google()
}

rootProject.name = "TwinMindSecondBrain"

include("base", "wireframe", "transcription", "website")
