import java.util.Properties

val localProperties = Properties()
localProperties.load(File(rootProject.projectDir, "local.properties").inputStream())

plugins {
    alias(libs.plugins.androidApplication)
}


android {
    namespace = "com.example.space_game_v2"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.space_game_v2"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val serverUrl = localProperties.getProperty("server.url", "")
        buildConfigField("String", "SERVER_URL", "\"$serverUrl\"")


        val secretKey = localProperties.getProperty("secret.key", "")
        buildConfigField("String", "SECRET_KEY", "\"$secretKey\"")


        val token = localProperties.getProperty("token", "")
        buildConfigField("String", "TOKEN", "\"$token\"")
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
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.java.jwt)
    implementation(libs.gson)
    implementation(libs.recyclerview)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}