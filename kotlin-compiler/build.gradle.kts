plugins {
    alias(libs.plugins.android.library)
}

val kotlinCompilerVersion: String by project

android {
    namespace = "dev.pranav.kotlin_compiler"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {


    //noinspection UseTomlInstead
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.1")
    implementation("org.codehaus.woodstox:stax2-api:4.3.0")

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinCompilerVersion")
    implementation("io.github.itsaky:nb-javac-android:17.0.0.3")
    implementation("org.jetbrains.intellij.deps:trove4j:1.0.20200330")
    implementation("org.jdom:jdom:2.0.2")
    implementation("com.google.guava:guava:33.6.0-android")
    implementation("org.jetbrains:annotations:26.1.0")

    //noinspection GradleDependency
    implementation("org.jetbrains.kotlin:kotlin-compiler:$kotlinCompilerVersion")
    compileOnly(project(":stubs"))

}
