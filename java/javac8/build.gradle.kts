plugins {
    id("com.android.library")
}

android {
    namespace = "dev.pranav.javac8"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    compileOnly(files("../javac/libs/nb-javac-26+35.jar"))

    //compileOnly(files("../javac/libs/nb-javac-26+35.jar"))
    implementation(project(":java:zipfs"))
    implementation(project(":java:jrtfs"))

}
tasks.withType<JavaCompile>().configureEach {
    //options.compilerArgs.add("-XDignore.symbol.file")
    //options.bootstrapClasspath = files(
    //    System.getenv("JAVA_HOME") + "/jre/lib/rt.jar",
    //    "../javac/libs/nb-javac-26+35.jar"
    //)
        options.compilerArgs.addAll(
            listOf(
                "-XDignore.symbol.file",
                "--patch-module",
                "jdk.compiler=${project.projectDir}/libs/nb-javac-26+35.jar"
            )
        )
}
