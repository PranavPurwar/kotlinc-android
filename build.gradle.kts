apply(plugin = "inject-kotlin-compiler-classes")
apply(plugin = "inject-java-compiler-classes")

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    `maven-publish`
}

val kotlinVersion: String = project.findProperty("kotlinCompilerVersion") as? String
    ?: throw IllegalStateException("kotlinCompilerVersion not set in gradle.properties")

val javaVersion: String = project.findProperty("javaCompilerVersion") as? String
    ?: throw IllegalStateException("javaCompilerVersion not set in gradle.properties")

val kPatchTask = tasks.named("patchKotlinCompilerJar")
kPatchTask.configure {
    outputs.upToDateWhen { false }
}

val jPatchTask = tasks.named("patchJavaCompilerJar")
jPatchTask.configure {
    outputs.upToDateWhen { false }
}

publishing {
    publications {
        create<MavenPublication>("patchedKotlinCompiler") {
            groupId = "dev.pranav.kotlin"
            artifactId = "kotlin-compiler-android"
            version = kotlinVersion

            val patchedJar = layout.buildDirectory.file("patchedKotlinCompilerJar/kotlin-compiler-${kotlinVersion}-patched.jar")
            artifact(patchedJar) { builtBy(kPatchTask) }

            pom.withXml {
                val root = asNode()
                val deps = root.appendNode("dependencies")

                fun addDep(group: String, artifact: String, ver: String) {
                    val d = deps.appendNode("dependency")
                    d.appendNode("groupId", group)
                    d.appendNode("artifactId", artifact)
                    d.appendNode("version", ver)
                }

                addDep("org.lsposed.hiddenapibypass", "hiddenapibypass", "6.1")
                addDep("com.google.guava", "guava", "33.6.0-android")
                addDep("org.codehaus.woodstox", "stax2-api", "4.3.0")
                addDep("org.jetbrains.kotlin", "kotlin-reflect", kotlinVersion)
                addDep("org.jetbrains.intellij.deps", "trove4j", "1.0.20200330")
                addDep("org.jetbrains.kotlinx", "kotlinx-collections-immutable", "0.4.0")
                addDep("org.jdom", "jdom", "2.0.2")
                addDep("org.antlr", "antlr4-runtime", "4.13.2")
                addDep("io.vavr", "vavr", "1.0.1")
                addDep("dev.pranav.java", "javac-android", javaVersion)
                addDep("com.github.Cosmic-IDE.kotlinc-android", "jaxp", "fce2462f00")

            }
        }
        create<MavenPublication>("patchedJavaCompiler") {
            groupId = "dev.pranav.java"
            artifactId = "javac-android"
            version = javaVersion

            val patchedJar = layout.buildDirectory.file("patchedJavaCompilerJar/nb-javac-${javaVersion}-patched.jar")
            artifact(patchedJar) { builtBy(jPatchTask) }

            pom.withXml {
                val root = asNode()
                val deps = root.appendNode("dependencies")

                fun addDep(group: String, artifact: String, ver: String) {
                    val d = deps.appendNode("dependency")
                    d.appendNode("groupId", group)
                    d.appendNode("artifactId", artifact)
                    d.appendNode("version", ver)
                }

                addDep("com.github.Cosmic-IDE.kotlinc-android", "jaxp", "fce2462f00")
            }
        }
    }

    repositories {
        mavenLocal()
    }
}

tasks.register("publishPatchedToMavenLocal") {
    group = "publishing"
    description = "Build patched kotlin-compiler and nb-javac jars and publish to mavenLocal"
    dependsOn(
        "publishPatchedJavaCompilerPublicationToMavenLocal",
        "publishPatchedKotlinCompilerPublicationToMavenLocal"
    )
}


