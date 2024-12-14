import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.ktlint)
}

group = "dev.robustum"
version = "0.1.0"

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven") {
                name = "Modrinth"
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

val testAgent = configurations.create("testAgent") {
    isCanBeConsumed = false
}

dependencies {
    minecraft(libs.minecraft)
    mappings("net.fabricmc:yarn:${libs.versions.fabric.yarn.get()}:v2")
    modImplementation(libs.bundles.mods.fabric)
    modLocalRuntime(libs.bundles.mods.debug)
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(libs.fabric.loader.junit)
    testAgent(project(path = ":test-agent", configuration = "agentJar"))
}

loom {
    accessWidenerPath = file("src/main/resources/robustum_core.accesswidener")

    mods {
        create("robustum_core") {
            sourceSet(sourceSets.main.get())
        }
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

ktlint {
    version = libs.versions.ktlint
    reporters {
        reporter(ReporterType.HTML)
        reporter(ReporterType.SARIF)
    }
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

tasks {
    test {
        useJUnitPlatform()
        val runDir = file("build/test_run")
        workingDir = runDir

        // Workaround https://github.com/FabricMC/fabric-loader/issues/817
        // Original: https://github.com/embeddedt/ModernFix/commit/03b23957827c42d5df5a11f3d07f807c5343e87e
        jvmArgs("-javaagent:${testAgent.singleFile.absolutePath}")
        dependsOn(testAgent)

        doFirst {
            runDir.mkdir()
        }
    }
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
    jar {
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }
    }
}
