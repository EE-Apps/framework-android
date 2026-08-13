plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.eenot.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        // applicationId не нужен — это библиотека
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.android)
}