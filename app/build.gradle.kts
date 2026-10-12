plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cifrabook"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cifrabook"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Versão estável do leitor de PDF via JitPack

    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
