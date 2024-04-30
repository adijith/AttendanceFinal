plugins {
    id ("com.android.library")
    alias(libs.plugins.jetbrainsKotlinAndroid) // Assuming libs.plugins.jetbrainsKotlinAndroid points to the Kotlin plugin
}

android {
    namespace = "com.example.data"
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding= true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx) // Assuming this dependency is needed for your data classes
    testImplementation(libs.junit) // Testing dependencies for your data module (optional)
}