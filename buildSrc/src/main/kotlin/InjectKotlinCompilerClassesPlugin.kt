package buildsrc.patcher

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin to inject compiled classes from :kotlinc-compiler into the official kotlin-compiler JAR,
 * then publish the patched JAR as a new Gradle module version in the local Maven repository.
 */
class InjectKotlinCompilerClassesPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("patchKotlinCompilerJar", PatchKotlinCompilerJarTask::class.java) {
            // Configure task if needed
        }
    }
}


