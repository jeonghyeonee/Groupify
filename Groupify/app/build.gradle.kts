import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.groupify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.groupify"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localPropertiesFile = File(rootDir, "local.properties")
        if (localPropertiesFile.exists()) {
            val properties = Properties().apply {
                load(FileInputStream(localPropertiesFile))
            }
            val sdkDir = properties.getProperty("sdk.dir")
            if (sdkDir != null) {
                buildConfigField("String", "SDK_DIR", "\"${sdkDir.replace("\\", "\\\\")}\"")
                println("SDK_DIR: ${sdkDir.replace("\\", "\\\\")}")
            } else {
                buildConfigField("String", "SDK_DIR", "\"\"")
                println("SDK_DIR is null in local.properties")
            }
        } else {
            buildConfigField("String", "SDK_DIR", "\"\"")
            println("local.properties file not found")
        }

        val osName = System.getProperty("os.name").toLowerCase()
        buildConfigField("Boolean", "IS_WINDOWS", "${osName.contains("win")}")
        buildConfigField("String", "PROJECT_DIR", "\"${rootDir.absolutePath.replace("\\", "\\\\")}\"")
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
        buildConfig = true
    }
}


dependencies {
    // Firebase BoM: 모든 Firebase 라이브러리 버전 관리를 일관되게 함
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase 및 Google Play 서비스
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")

    // 이미지 로딩 및 Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // AndroidX 및 기타 라이브러리
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // JSON 파싱
    implementation("com.google.code.gson:gson:2.8.8")

    // Retrofit 및 Gson 변환기
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Kotlin Runtime
    implementation(kotlin("script-runtime"))

    // 테스트 라이브러리
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // 애니메이션 라이브러리
    implementation("com.airbnb.android:lottie:4.2.2")

    // dotLottie 라이브러리 추가
    // implementation("com.github.LottieFiles:dotlottie-android:0.0.3")
}
