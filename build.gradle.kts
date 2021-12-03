import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

repositories {
    mavenCentral()
}

group = "com.github.holgerbrandl.kscript.launcher"

val kotlinVersion: String = "1.5.31"

tasks.test {
    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.FAILED); exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<Test> {
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) { logger.quiet("\nTest class: ${suite.displayName}") }
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            logger.quiet("${String.format( "%-60s - %-10s", testDescriptor.name, result.resultType )} ")
        }

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {}
    })
}


dependencies {
    implementation("com.offbytwo:docopt:0.6.0.20150202")

    implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    //WARN: resolving artifacts with kotlin-scripting in 1.5.31 doesn't work, so that's why 1.4.32
    //Alternative is Maven Archeologist: https://github.com/square/maven-archeologist
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:1.4.32")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies:1.4.32")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven:1.4.32")

    implementation("commons-io:commons-io:2.11.0")
    implementation("commons-codec:commons-codec:1.15")

    implementation("org.slf4j:slf4j-nop:1.7.32")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("io.mockk:mockk:1.12.1")

    testImplementation(kotlin("script-runtime"))
}

val shadowJar by tasks.getting(ShadowJar::class) {
    // set empty string to classifier and version to get predictable jar file name: build/libs/kscript.jar
    archiveFileName.set("kscript.jar")
    doLast {
        copy {
            from(File(projectDir, "src/kscript"))
            into(archiveFile.get().asFile.parentFile)
        }
    }
    manifest {
        attributes("Main-Class" to "kscript.app.KscriptKt")
    }
}

// Disable standard jar task to avoid building non-shadow jars
val jar by tasks.getting {
    enabled = false
}
// Build shadowJar when
val assemble by tasks.getting {
    dependsOn(shadowJar)
}

val test by tasks.getting {
    inputs.dir("${project.projectDir}/test/resources")
}
