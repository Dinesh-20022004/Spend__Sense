plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // This plugin is required for Room's annotation processing
}

android {
    namespace = "com.example.spendsense"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.spendsense"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Core Android Libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navigation (already present)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Lifecycle, ViewModel, and LiveData for MVVM
    val lifecycle_version = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.activity:activity-ktx:1.9.0") // Needed for the by viewModels() delegate
    implementation("androidx.fragment:fragment-ktx:1.6.2") // Needed for the by viewModels() delegate

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version") // Annotation processor
    implementation("androidx.room:room-ktx:$room_version") // For Coroutines support

    // Coroutines for background tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Chart Library (already present)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // JSON Serialization (already present)
    implementation("com.google.code.gson:gson:2.10.1")

    // Preference Library (already present)
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Testing Libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}