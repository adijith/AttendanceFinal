plugins {
    id ("com.android.library")
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services") // Assuming you still need Firebase
}

android {
    namespace = "com.example.admin"
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
    implementation(libs.androidx.appcompat) // Optional, for compatibility or Material Design features
    implementation(libs.material) // Optional, for Material Design components
    implementation(libs.firebase.database) // Assuming you use Firebase Database (optional)
    implementation(libs.firebase.auth) // Assuming you use Firebase Authentication (optional)
    implementation(project(":data")) // Dependency on your data module (assuming it's a library module)
    testImplementation(libs.junit) // Testing dependencies for your library module (optional)
}