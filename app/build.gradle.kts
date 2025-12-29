plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.tokendad.nesventorynew"
    compileSdk = 36

    val versionFile = rootProject.file("VERSION")
    val versionText = if (versionFile.exists()) versionFile.readText().trim() else "1.0.0"

    defaultConfig {
        applicationId = "com.tokendad.nesventorynew"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = versionText

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // These env variables are filled by the GitHub Action
            storeFile = file("release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // This links the release build to the signing config above
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            // Fixes the "Unable to strip libraries" warning in debug builds
            packaging {
                jniLibs {
                    keepDebugSymbols.add("**/*.so")
                }
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.datastore.preferences)

    // Hilt core and compiler
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kaptTest(libs.hilt.compiler)
    kaptAndroidTest(libs.hilt.compiler)

    // Hilt testing
    testImplementation(libs.hilt.testing)
    androidTestImplementation(libs.hilt.testing)

    // Hilt integration with Compose Navigation
    implementation(libs.androidx.hilt.navigation.compose)

    // Jetpack Navigation
    implementation(libs.androidx.navigation.compose)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Coil
    implementation(libs.coil.compose)
}