val releaseGroup: String by project

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "$releaseGroup.base"
    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    api(libs.kotlinx.coroutines)
    api(libs.material)
    api(libs.androidx.core.ktx)
    api(libs.androidx.multidex)
    api(libs.roundedimageview)
}
