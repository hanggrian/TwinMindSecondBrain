import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val releaseGroup: String by project
val releaseArtifact: String by project
val releaseVersion: String by project

val javaCompileVersion = JavaLanguageVersion.of(libs.versions.java.compile.get())
val javaSupportVersion = JavaLanguageVersion.of(libs.versions.java.support.get())

allprojects {
    group = releaseGroup
    version = releaseVersion
}

plugins {
    alias(libs.plugins.android.application)
    kotlin("android") version libs.versions.kotlin
    alias(libs.plugins.ktlint.gradle)
}

kotlin.jvmToolchain(javaCompileVersion.asInt())

ktlint.version.set(libs.versions.ktlint.get())

android {
    namespace = "$releaseGroup.$releaseArtifact"
    testNamespace = "$namespace.test"
    compileSdk = libs.versions.android.compile.get().toInt()
    defaultConfig {
        targetSdk = libs.versions.android.compile.get().toInt()
        minSdk = libs.versions.android.support.get().toInt()
        version = releaseVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        applicationId = namespace
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaSupportVersion)
        targetCompatibility = JavaVersion.toVersion(javaSupportVersion)
    }
    kotlinOptions {
        jvmTarget = JavaVersion.toVersion(javaSupportVersion).toString()
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    ktlintRuleset(libs.rulebook.ktlint)

    implementation(libs.kotlinx.coroutines)
    implementation(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.roundedimageview)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget
        .set(JvmTarget.fromTarget(JavaVersion.toVersion(javaSupportVersion).toString()))
}
