plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.tiktok"
    compileSdk = 35

    defaultConfig {
        vectorDrawables.useSupportLibrary = true
        applicationId = "com.example.tiktok"
        minSdk = 28
        targetSdk = 35
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

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    //chuyển list -> chuỗi json
    implementation ("com.google.code.gson:gson:2.10.1")

    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation(libs.firebase.storage)

    val camerax_version = "1.2.3"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-video:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-extensions:${camerax_version}")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Glide cho load ảnh
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // Circular ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //Exo for video
    implementation(libs.exoplayer)

    //Lib for okhttp
    implementation(libs.retrofit) // Retrofit core
    implementation(libs.converter.gson) // Gson converter for Retrofit
    implementation(libs.okhttp) // OkHttp core library
    implementation(libs.okhttp.urlconnection)

    //Exo for video
    implementation(libs.exoplayer)
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
}

