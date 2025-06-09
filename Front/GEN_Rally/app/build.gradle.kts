plugins {
    id("com.android.application")
    id("io.freefair.lombok") version "8.4"
}

android {
    namespace = "com.example.rally"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rally"
        minSdk = 24
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
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    implementation ("com.google.android.gms:play-services-maps:19.2.0")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.android.libraries.places:places:4.2.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")  // Retrofit
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")   // JSON 컨버터
    implementation ("com.github.bumptech.glide:glide:4.16.0")    // gif 파일
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    compileOnly("org.projectlombok:lombok:1.18.30")       // lombok
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}