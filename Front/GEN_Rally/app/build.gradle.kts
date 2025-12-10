import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("io.freefair.lombok") version "8.4"
    id("com.google.gms.google-services") version "4.4.3"
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.rally"
    compileSdk = 35
    buildFeatures { buildConfig = true}

    defaultConfig {
        applicationId = "com.example.rally"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
        buildConfigField ("String", "WS_BASE_URL", "\"ws://10.0.2.2:8080/stomp\"")

        val kakaoKey = localProperties.getProperty("kakao.api.key") ?: ""
        val naverId = localProperties.getProperty("naver.client.id") ?: ""
        val naverSecret = localProperties.getProperty("naver.client.secret") ?: ""

        buildConfigField("String", "KAKAO_APP_KEY", "\"$kakaoKey\"")
        buildConfigField("String", "NAVER_CLIENT_ID", "\"$naverId\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"$naverSecret\"")
        manifestPlaceholders["KAKAO_APP_KEY"] = kakaoKey
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
    implementation ("androidx.security:security-crypto:1.1.0-alpha03") // EncryptedSharedPreferences
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    compileOnly("org.projectlombok:lombok:1.18.30")       // lombok
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation ("com.google.android.gms:play-services-wearable:18.2.0")
    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation (platform("com.google.firebase:firebase-bom:34.2.0")) // Firebase
    implementation ("com.google.firebase:firebase-storage")
    implementation ("com.google.firebase:firebase-messaging-ktx:23.4.1")
    implementation ("com.github.NaikSoftware:StompProtocolAndroid:1.6.6") // STOMP
    implementation ("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation ("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0") // Chart
    implementation ("com.github.prolificinteractive:material-calendarview:2.0.1") // calendar
    implementation ("com.kakao.sdk:v2-user:2.15.0") // 카카오 SDK
    implementation ("com.navercorp.nid:oauth:5.9.1") // 네이버 SDK
}