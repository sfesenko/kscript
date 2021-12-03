package kscript.app.creator

import kscript.app.appdir.Cache
import kscript.app.code.GradleTemplates
import kscript.app.code.Templates
import kscript.app.model.Script
import kscript.app.util.FileUtils
import kscript.app.util.Logger.infoMsg
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class PackageCreator(private val cache: Cache, private val executor: Executor) {
    /**
     * Create and use a temporary gradle project to package the compiled script using capsule.
     * See https://github.com/puniverse/capsule
     */
    fun packageKscript(script: Script, jarArtifact: JarArtifact) {
        infoMsg("Packaging script '${script.scriptName}' into standalone executable...")

        val projectDir = cache.findOrCreatePackage(script.digest)

        // create exec_header to allow for direction execution (see http://www.capsule.io/user-guide/#really-executable-capsules)
        // from https://github.com/puniverse/capsule/blob/master/capsule-util/src/main/resources/capsule/execheader.sh
        FileUtils.createFile(projectDir.resolve("exec_header.sh"), Templates.executeHeader)
        FileUtils.createFile(
            projectDir.resolve("build.gradle.kts"),
            GradleTemplates.createGradlePackageScript(script, jarArtifact)
        )

        executor.createPackage(projectDir)

        projectDir.resolve("build/libs/appName").toFile().setExecutable(true)

        infoMsg("Finished packaging '${script.scriptName}'; executable path: ${projectDir}/build/libs/")
    }

    /**
     * Merge jar files
     */
    fun packageKscript(script: Script, scriptJar: JarArtifact, resolvedDependencies: Set<Path>) {
        val artifactName = "${script.scriptName}.jar"
        infoMsg("Packaging script '$artifactName' into standalone executable...")
        JarOutputStream(FileOutputStream(artifactName)).use { outJarFile ->
            val entries = mutableSetOf<String>()
            fun append(jarPath: Path, allowManifest: Boolean = false) =
                JarFile(jarPath.toFile()).use { jar ->
                    jar.versionedStream()
                        .filter { !it.name.startsWith("META-INF/") || allowManifest }
                        .filter { it.name !in entries }
                        .forEach { jarEntry ->
                            entries += jarEntry.name
                            outJarFile.putNextEntry(jarEntry)
                            jar.getInputStream(jarEntry).copyTo(outJarFile)
                        }
                    infoMsg("package $jarPath")
                }
            append(scriptJar.path, allowManifest = true)
            resolvedDependencies.forEach(::append)
        }
        infoMsg("Finished packaging '$artifactName'")
    }
}
