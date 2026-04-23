package buildsrc.patcher

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

open class PatchNBJavacTask: DefaultTask() {

    @get:Input
    val javaCompilerVersion: Property<String> =
        project.objects.property(String::class.java).apply {
            project.findProperty("javaCompilerVersion")?.let { set(it as String) }
        }

    @get:Input
    val sourceProjectPaths: ListProperty<String> =
        project.objects.listProperty(String::class.java).apply {
            set(listOf(":java:javac", ":java:javac8", ":java:zipfs", ":java:jrtfs"))

            project.findProperty("sourceProjectPaths")?.let {
                set((it as String).split(",").map { it.trim() })
            }
        }

    @get:Internal
    val sourceProjects: List<Project>
        get() = sourceProjectPaths.get().mapNotNull { path ->
            val proj = project.rootProject.findProject(path)
            if (proj == null) {
                println("⚠️ Warning: Project not found: $path")
            }
            proj
        }

    @get:Input
    @get:Optional
    val stripPackages: Property<String> =
        project.objects.property(String::class.java).apply {
            project.findProperty("stripPackages")?.let { set(it as String) }
        }

    @get:Input
    @get:Optional
    val stripResources: Property<String> =
        project.objects.property(String::class.java).apply {
            project.findProperty("stripResources")?.let { set(it as String) }
        }

    @get:OutputDirectory
    val outputDir: DirectoryProperty =
        project.objects.directoryProperty()
            .convention(project.layout.buildDirectory.dir("patchedJavaCompilerJar"))

    private val layout: ProjectLayout = project.layout

    init {
        dependsOn(
            sourceProjectPaths.map { paths ->
                paths.map {
                    if (it.endsWith("zipfs")) {
                        "$it:classes"
                    } else {
                        "$it:compileDebugJavaWithJavac"
                    }
                }
            }
        )
    }

    @TaskAction
    fun patchJar() {
        val version = javaCompilerVersion.orNull
            ?: throw IllegalArgumentException(
                "Set javaCompilerVersion in gradle.properties or with -PjavaCompilerVersion"
            )

        val compilerJar = resolveCompilerJar(version)

        val classesDirs = findSourceClassesDirs().filter { it.exists() && it.isDirectory }
        if (classesDirs.isEmpty()) {
            throw IllegalStateException(
                "No compiled classes found for projects ${sourceProjectPaths.get()}"
            )
        }

        val unpackedDir = outputDir.get().asFile.resolve("unpacked").apply {
            deleteRecursively()
            mkdirs()
        }

        unpackJar(compilerJar, unpackedDir)

        // Order matters: later projects override earlier ones
        classesDirs.forEach {
            println("Copying classes from: ${it.absolutePath}")
            copyClasses(it, unpackedDir)
        }

        val patchedJar = outputDir.get().asFile.resolve("nb-javac-$version-patched.jar")
        repackJar(unpackedDir, patchedJar)

        println("✅ Patched jar written to: ${patchedJar.absolutePath}")
    }

    private fun resolveCompilerJar(version: String): File {
        val jar = project.rootProject.file("java/javac/libs/nb-javac-26+35.jar")

        if (!jar.exists()) {
            throw IllegalStateException("Local JAR not found at: ${jar.absolutePath}")
        }

        return jar
    }

    private fun findSourceClassesDirs(): List<File> {
        val result = mutableListOf<File>()

        sourceProjects.forEach { srcProj ->
            val buildDir = srcProj.layout.buildDirectory.asFile.get()
            val resolved = File(buildDir, "intermediates/javac/debug/compileDebugJavaWithJavac/classes")

            println("Source Directory for patches (${srcProj.path}): ${resolved.absolutePath}")

            if (resolved.exists()) {
                result.add(resolved)
            } else {
                buildDir.walkTopDown()
                    .filter { it.isFile && it.extension == "class" }
                    .mapNotNull { classFile ->
                        var current: File? = classFile.parentFile
                        while (current != null && current.toPath().startsWith(buildDir.toPath())) {
                            if (current.name == "classes") {
                                return@mapNotNull current
                            }
                            current = current.parentFile
                        }
                        classFile.parentFile
                    }
                    .distinct()
                    .filter { it.exists() && it.isDirectory }
                    .forEach { result.add(it) }
            }
        }

        return result.distinct()
    }

    private fun unpackJar(jar: File, destDir: File) {
        project.copy {
            from(project.zipTree(jar))
            into(destDir)
        }

        destDir.resolve("com/sun/tools/javac/resources/version.properties-template").apply {
            resolveSibling("version.properties").writeText(
                """
jdk=26-ea+35
full=26-ea+35
release=26-ea+35
                """.trimIndent()
            )
        }
    }

    private fun copyClasses(classesDir: File, unpackedDir: File) {
        classesDir.walkTopDown().forEach { sourceFile ->
            val rel = sourceFile.relativeTo(classesDir)
            val dest = File(unpackedDir, rel.path)

            if (sourceFile.isDirectory) {
                dest.mkdirs()
            } else {
                dest.parentFile.mkdirs()
                sourceFile.copyTo(dest, overwrite = true)
            }
        }
    }

    private fun repackJar(unpackedDir: File, patchedJar: File) {
        project.ant.invokeMethod(
            "zip",
            mapOf(
                "destfile" to patchedJar,
                "basedir" to unpackedDir
            )
        )
    }
}
