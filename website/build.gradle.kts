val developerId: String by project
val developerName: String by project
val developerUrl: String by project
val releaseDescription: String by project
val releaseUrl: String by project

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.pages)
    alias(libs.plugins.git.publish)
}

dokka.dokkaPublications.html {
    outputDirectory.set(layout.buildDirectory.dir("dokka/dokka/"))
}

pages {
    resources.from("images", layout.buildDirectory.dir("dokka"))
    favicon.set("favicon.ico")
    materialist {
        authorName = developerName
        authorUrl = developerUrl
        projectName = rootProject.name
        projectDescription = releaseDescription
        projectUrl = releaseUrl
        button("View\nDocumentation", "dokka/")
        button("Visit\nGitHub", releaseUrl)

        colorSurface = "linear-gradient(45deg, #012840 0%, #014873 50%, #FF7604 100%)"
        colorSurfaceContainer = "#FFFFFF"
        colorOnSurface = "#0B4F75"
        colorPrimary = "#F27128"
        colorSecondary = "#FF9A5C"
    }
}

gitPublish {
    repoUri.set("git@github.com:$developerId/${rootProject.name}.git")
    branch.set("gh-pages")
    contents.from(pages.outputDirectory)
}

dependencies.dokka(project(":transcription"))

tasks.deployResources {
    dependsOn(tasks.dokkaGeneratePublicationHtml)
}
