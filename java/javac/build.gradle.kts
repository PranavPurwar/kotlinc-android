plugins {
    id("com.android.library")
}

android {
    namespace = "dev.pranav.javac"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly(files("../javac/libs/nb-javac-26+35.jar"))
    implementation(project(":java:javac8"))
    implementation(project(":java:zipfs"))
    implementation(project(":java:jrtfs"))
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-XDignore.symbol.file")
}
