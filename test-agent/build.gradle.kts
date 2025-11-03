import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version libs.versions.kotlin
}

group = "dev.robustum"
version = "1.0"
base {
    archivesName = "test-agent"
}

val agentJar = configurations.register("agentJar") {
    isCanBeConsumed = true
    isCanBeResolved = false
}

repositories {
    mavenCentral()
    maven(url = "https://maven.fabricmc.net/") {
        name = "Fabric"
    }
}

dependencies {
    compileOnly(libs.fabric.loader)
    implementation(libs.bundles.asm)
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

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    jar {
        manifest {
            attributes(
                "Premain-Class" to "org.embeddedt.modernfix.testing.Agent",
                "Can-Redefine-Classes" to "true",
                "Can-Set-Native-Method-Prefix" to "true",
            )
        }
    }

    // fabric-loomに対するパッチのオリジナルはLGPL-3.0ライセンスなのでリスクを回避するために必要に応じてダウンロードする
    // https://github.com/embeddedt/ModernFix?tab=License-1-ov-file#readme
    // https://github.com/embeddedt/ModernFix/commit/03b23957827c42d5df5a11f3d07f807c5343e87e#diff-bb0eb72bce858352a965127feb642536626f7b5bc911482becf330cb173672f4
    val downloadCodes = register("downloadCodes") {
        doFirst {
            val projectDir = file("src/main/java/org/embeddedt/modernfix/testing")
            projectDir.mkdirs()
            val agent = projectDir.resolve("Agent.java")
            if (!agent.exists()) {
                uri(
                    "https://raw.githubusercontent.com/embeddedt/ModernFix/03b23957827c42d5df5a11f3d07f807c5343e87e/test_agent/src/main/java/org/embeddedt/modernfix/testing/Agent.java",
                ).toURL().openStream().use {
                    agent.writeBytes(it.readBytes())
                }
            }
            val agentHooks = projectDir.resolve("AgentHooks.java")
            if (!agentHooks.exists()) {
                uri(
                    "https://raw.githubusercontent.com/embeddedt/ModernFix/03b23957827c42d5df5a11f3d07f807c5343e87e/test_agent/src/main/java/org/embeddedt/modernfix/testing/AgentHooks.java",
                ).toURL()
                    .openStream()
                    .use {
                        agentHooks.writeBytes(it.readBytes())
                    }
            }
        }
    }
    checkKotlinGradlePluginConfigurationErrors {
        dependsOn(downloadCodes)
    }
}

artifacts {
    add(agentJar.name, tasks.jar)
}
