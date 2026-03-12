plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")

}

android {
    namespace = "com.example.spprojectsqlitetask"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.spprojectsqlitetask"
        minSdk = 28
        targetSdk = 36
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
    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    // Room components
    val room_version = "2.8.4"
    // Основная библиотека Room
    implementation("androidx.room:room-runtime:$room_version")
    // Компилятор для обработки аннотаций
    // Для Kotlin используем KSP (быстрее чем kapt)
    kapt("androidx.room:room-compiler:$room_version")
    // Поддержка Kotlin корутин (suspend функции в DAO)
    implementation("androidx.room:room-ktx:$room_version")
    // Поддерка Flow // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${room_version}")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Для использования с Activity/Fragment
    implementation("androidx.activity:activity-ktx:1.8.0")

    implementation("androidx.fragment:fragment-ktx:1.6.0")
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.work:work-runtime-ktx:2.9.0")


}