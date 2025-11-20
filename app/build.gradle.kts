plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
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
                "proguard-rules.pro"   // ✅ 작은따옴표 → 큰따옴표로 고침
            )
        }
    }

    buildFeatures {
        dataBinding = true      // 데이터바인딩 사용 시
        viewBinding = true      // 뷰바인딩도 같이 쓰면 편함
        // compose = true        // Jetpack Compose 쓸 때만
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
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // ✅ MPAndroidChart (그래프용)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Retrofit 라이브러리 (HTTP 통신 클라이언트)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // JSON 데이터를 Kotlin/Java 객체로 변환해주는 컨버터
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Coroutines (비동기 처리)를 위한 어댑터 (Kotlin 사용 시 추천)
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")
    implementation("androidx.activity:activity-ktx:1.9.0")
}
