import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

val releaseGroup: String by project
val releaseVersion: String by project

val javaCompileVersion = JavaLanguageVersion.of(libs.versions.java.compile.get())
val javaSupportVersion = JavaLanguageVersion.of(libs.versions.java.support.get())

allprojects {
    group = releaseGroup
    version = releaseVersion
}

subprojects {
    plugins.withType<LibraryPlugin>().configureEach {
        modify(the<LibraryExtension>())
    }
    plugins.withType<AppPlugin>().configureEach {
        modify(the<BaseAppModuleExtension>())
    }
    plugins.withType<KotlinAndroidPluginWrapper>().configureEach {
        the<KotlinAndroidProjectExtension>().jvmToolchain(javaCompileVersion.asInt())
    }
    plugins.withType<KtlintPlugin>().configureEach {
        the<KtlintExtension>().version.set(libs.versions.ktlint.get())
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget
            .set(JvmTarget.fromTarget(JavaVersion.toVersion(javaSupportVersion).toString()))
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    kotlin("android") version libs.versions.kotlin apply false
    kotlin("plugin.compose") version libs.versions.kotlin apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint.gradle) apply false
}

fun modify(extension: BaseExtension) {
    extension.setCompileSdkVersion(libs.versions.android.compile.get().toInt())
    extension.defaultConfig {
        targetSdk = libs.versions.android.compile.get().toInt()
        minSdk = libs.versions.android.support.get().toInt()
        version = releaseVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        applicationId = extension.namespace
    }
    extension.compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaSupportVersion)
        targetCompatibility = JavaVersion.toVersion(javaSupportVersion)
    }
}
