package buildsrc.patcher

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

open class PatchKotlinCompilerJarTask : DefaultTask() {
    @get:Input
    val kotlinCompilerVersion: Property<String> = project.objects.property(String::class.java).apply {
        project.findProperty("kotlinCompilerVersion")?.let { set(it as String) }
    }

    @get:Input
    val sourceProjectPath: Property<String> = project.objects.property(String::class.java).apply {
        set(project.findProperty("patchSourceProject") as? String ?: ":kotlin:kotlin-compiler")
    }

    @get:Internal
    val sourceProject: Project? get() = resolveSourceProject()

    @get:Input
    @get:Optional
    val stripPackages: Property<String> = project.objects.property(String::class.java).apply {
        project.findProperty("stripPackages")?.let { set(it as String) }
    }

    @get:Input
    @get:Optional
    val stripResources: Property<String> = project.objects.property(String::class.java).apply {
        project.findProperty("stripResources")?.let { set(it as String) }
    }

    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty().convention(project.layout.buildDirectory.dir("patchedKotlinCompilerJar"))

    private val layout: ProjectLayout = project.layout


    init {
        dependsOn(
            sourceProjectPath.map { "$it:compileDebugJavaWithJavac" }
        )
    }

    @TaskAction
    fun patchJar() {
        val version = kotlinCompilerVersion.orNull ?: throw IllegalArgumentException("Set kotlinCompilerVersion in gradle.properties or with -PkotlinCompilerVersion")
        val kotlinJar = resolveKotlinCompilerJar(version)

        val classesDirs = findSourceClassesDirs().filter { it.exists() && it.isDirectory }
        if (classesDirs.isEmpty()) {
            throw IllegalStateException("No compiled classes found for source project '${sourceProjectPath.get()}'. Build the source project first or set patchSourceProject to the correct module.")
        }

        val unpackedDir = outputDir.get().asFile.resolve("unpacked").apply { mkdirs() }

        unpackJar(kotlinJar, unpackedDir)
        classesDirs.forEach { copyClasses(it, unpackedDir) }
        stripPackagesAndResources(unpackedDir)
        createResources(unpackedDir)

        val patchedJar = outputDir.get().asFile.resolve("kotlin-compiler-$version-patched.jar")
        repackJar(unpackedDir, patchedJar)
        println("Patched jar written to: ${patchedJar.absolutePath}")
    }

    private fun resolveKotlinCompilerJar(version: String): File {
        val dep = "org.jetbrains.kotlin:kotlin-compiler:$version"
        val configuration = project.configurations.detachedConfiguration(project.dependencies.create(dep))
        return configuration.resolve().firstOrNull { it.name == "kotlin-compiler-$version.jar" }
            ?: throw IllegalStateException("kotlin-compiler JAR not found for version $version")
    }

    private fun resolveSourceProject(): Project? {
        val srcPath = sourceProjectPath.get()
        return project.rootProject.findProject(srcPath) ?: project.rootProject.findProject(":kotlin:kotlin-compiler")
    }

    private fun findSourceClassesDirs(): List<File> {
        val srcProj = sourceProject ?: return emptyList()
        val buildDir = srcProj.layout.buildDirectory.asFile.get()

        val candidateDirs = listOf(
            File(buildDir, "intermediates/built_in_kotlinc/release/compileReleaseKotlin/classes"),
            File(buildDir, "intermediates/javac/release/compileReleaseJavaWithJavac/classes"),
        )

        val resolved = candidateDirs.filter { it.exists() && it.isDirectory }
        if (resolved.isNotEmpty()) return resolved

        return buildDir.walkTopDown()
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
            .toList()
    }

    private fun unpackJar(jar: File, destDir: File) {
        project.copy { from(project.zipTree(jar)); into(destDir) }
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

    private fun stripPackagesAndResources(unpackedDir: File) {
        val defaultPackages = listOf(
            "com/google/common",
            "org/codehaus/stax2",
            "org/jetbrains/annotations",
            "org/jdom",
            "kotlinx/collections",
            "org/antlr/v4/runtime",
            "io/vavr",
            "kotlin"
        )
        val packages = stripPackages.orNull?.split(',')?.map { it.trim() } ?: defaultPackages
        packages.forEach { pkg -> File(unpackedDir, pkg).takeIf { it.exists() }?.deleteRecursively() }

        val defaultResources = listOf("kotlin/internal/internal.kotlin_builtins")
        val resources = stripResources.orNull?.split(',')?.map { it.trim() } ?: defaultResources
        resources.forEach { res -> File(unpackedDir, res).takeIf { if (it.isFile) it.delete() else it.deleteRecursively() } }

        val nativeFolders = listOf(
            "darwin-aarch64",
            "darwin-x86-64",
            "freebsd-x86",
            "freebsd-x86-64",
            "linux-mips64el",
            "linux-ppc",
            "linux-ppc64le",
            "linux-riscv64",
            "linux-s390x",
            "openbsd-x86",
            "openbsd-x86-64",
            "sunos-sparc",
            "sunos-sparcv9",
            "sunos-x86",
            "sunos-x86-64",
            "win32",
            "win32-aarch64",
            "win32-x86",
            "win32-x86-64"
        )

        unpackedDir.listFiles()?.forEach { dir ->
            if (dir.isDirectory && dir.name in nativeFolders) {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.endsWith(".so")) {
                        file.delete()
                    }
                }
            }
        }
    }

    private fun createResources(unpackedDir: File) {
        // Required by runtime
        unpackedDir.resolve("org/jetbrains/kotlin/cli/common/CompilerSystemProperties.clazz").createNewFile()
        unpackedDir.resolve("org/jetbrains/kotlin/utils/PathUtil.clazz").createNewFile()
    }

    private fun repackJar(unpackedDir: File, patchedJar: File) {
        project.ant.invokeMethod("zip", mapOf("destfile" to patchedJar, "basedir" to unpackedDir))
    }
}


