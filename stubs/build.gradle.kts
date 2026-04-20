val kotlinCompilerVersion: String by project

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11

        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler:$kotlinCompilerVersion")

}
