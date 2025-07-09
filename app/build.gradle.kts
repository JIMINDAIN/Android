plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services") // Google Services 플러그인 추가
}

android {
    namespace = "com.example.mentalnote"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mentalnote"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)  // 여기서도 alias로 관리
    implementation(libs.androidx.core.ktx)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // 중복 제거: kotlinx-serialization-json 버전 하나만 남기기
    // implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") 제거

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.google.accompanist:accompanist-permissions:0.30.1")
    
    // 중복 제거: Compose material3, ui, ui-tooling-preview 버전 일치 확인하고 libs에 등록하는게 좋음
    // implementation ("androidx.compose.material3:material3:1.1.0")
    // implementation ("androidx.compose.ui:ui:1.4.3")
    // implementation ("androidx.compose.ui:ui-tooling-preview:1.4.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.calendarView)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Firebase BoM 및 Authentication 라이브러리 추가
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    }
