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

    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))

    // Add the dependencies for the App Check libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-appcheck-playintegrity")



    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.0")
    implementation("com.google.firebase:firebase-appcheck-playintegrity:18.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics:22.0.2")
    implementation("com.google.firebase:firebase-storage:21.0.0")

    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.0")
    implementation("com.google.code.gson:gson:2.8.8") // JSON 파싱을 위해

    implementation(kotlin("script-runtime"))

}
