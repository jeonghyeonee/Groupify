import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")  // kapt 플러그인 추가
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

        // local.properties 파일에서 sdk.dir 값을 읽어와서 BuildConfig에 추가
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
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("com.android.volley:volley:1.2.1")

    // Firebase Dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Retrofit for network requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    // Kotlin script runtime
    implementation(kotlin("script-runtime"))

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0") // 최신 버전을 확인하고 사용할 수도 있습니다.
}
