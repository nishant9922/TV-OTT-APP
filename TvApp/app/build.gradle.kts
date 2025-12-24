plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.tvtvapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tvtvapp"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

    }
    buildFeatures {
        viewBinding=true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }



}

dependencies {
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.7.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.androidx.activity)
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // REMOVE old Exoplayer completely
    // implementation("com.google.android.exoplayer:exoplayer:2.19.0")
    // implementation("com.google.android.exoplayer:exoplayer-ui:2.19.0")

    // Media3 (REQUIRED by LogixPlayer)
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
    implementation("androidx.media3:media3-common:1.5.1")
    // REQUIRED by LogixPlayer to avoid NoClassDefFoundError for DashManifest
    implementation("androidx.media3:media3-exoplayer-dash:1.5.1")


    // Logix Player AAR
    implementation(files("libs/LogixPlayerAndroid-m3-v1.0.3-debug.aar"))
}
