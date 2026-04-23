plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "dev.pranav.kotlincompiler"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "dev.pranav.kotlincompiler"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources.excludes += "README.md"
        resources.excludes += "SECURITY.md"
    }
}

dependencies {
    val kotlinCompilerVersion: String by project
    val javaCompilerVersion: String by project

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    implementation("com.github.Cosmic-IDE.kotlinc-android:jaxp:fce2462f00")

    implementation("dev.pranav.kotlin:kotlin-compiler-android:$kotlinCompilerVersion")
    implementation("dev.pranav.java:javac-android:$javaCompilerVersion")
}
