package buildsrc.patcher

import org.gradle.api.Plugin
import org.gradle.api.Project

class InjectJavaCompilerClassesPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("patchJavaCompilerJar", PatchNBJavacTask::class.java) {
            // Configure task if needed
        }
    }
}
