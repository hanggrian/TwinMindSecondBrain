val developerId: String by project
val developerName: String by project
val developerUrl: String by project
val releaseDescription: String by project
val releaseUrl: String by project

plugins {
    alias(libs.plugins.pages)
    alias(libs.plugins.git.publish)
}

pages {
    resources.from("images")
    favicon.set("favicon.ico")
    materialist {
        authorName = developerName
        authorUrl = developerUrl
        projectName = rootProject.name
        projectDescription = releaseDescription
        projectUrl = releaseUrl

        colorSurface = "linear-gradient(45deg, #012840 0%, #014873 50%, #FF7604 100%)"
        colorSurfaceContainer = "#FFFFFF"
        colorOnSurface = "#0B4F75"
        colorPrimary = "#F27128"
    }
}

gitPublish {
    repoUri.set("git@github.com:$developerId/${rootProject.name}.git")
    branch.set("gh-pages")
    contents.from(pages.outputDirectory)
}

tasks.register(LifecycleBasePlugin.CLEAN_TASK_NAME) {
    delete(layout.buildDirectory)
}
