val releaseGroup: String by project

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    kotlin("android") version libs.versions.kotlin
    kotlin("plugin.compose") version libs.versions.kotlin
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint.gradle)
}

android {
    namespace = "$releaseGroup.transcription"
    testNamespace = "$namespace.test"
    room.schemaDirectory("$projectDir/schemas")
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }
    buildTypes {
        debug {
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    ktlintRuleset(libs.rulebook.ktlint)

    implementation(project(":base"))
    implementation(libs.androidx.datastore)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.androidx.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.hilt)

    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(kotlin("test-junit", libs.versions.kotlin.get()))
    testImplementation(libs.bundles.junit4)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
