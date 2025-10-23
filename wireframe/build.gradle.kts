val releaseGroup: String by project

plugins {
    alias(libs.plugins.android.application)
    kotlin("android") version libs.versions.kotlin
    alias(libs.plugins.ktlint.gradle)
}

android {
    namespace = "$releaseGroup.wireframe"
    buildFeatures {
        buildConfig = false
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    ktlintRuleset(libs.rulebook.ktlint)

    implementation(project(":base"))
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
}
