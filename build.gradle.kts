@file:Suppress("VulnerableLibrariesLocal")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "org.javamaster"
version = "8.5.1"

repositories {
    maven { url = URI("https://maven.aliyun.com/nexus/content/groups/public/") }
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

allprojects {
    repositories {
        maven { url = URI("https://maven.aliyun.com/nexus/content/groups/public/") }
        mavenCentral()
    }
}

val ideaVersion = "2024.3"

dependencies {
    intellijPlatform {
        create("IC", ideaVersion)
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        bundledPlugin("com.intellij.java")
        bundledPlugin("com.intellij.modules.json")
        bundledModule("com.intellij.modules.json")
        plugin("ris58h.webcalm", "0.12.1")
    }

    implementation("org.mozilla:rhino:1.7.15")
    implementation("com.github.javafaker:javafaker:1.0.2")
    implementation("com.jayway.jsonpath:json-path:2.9.0")

    compileOnly("com.alibaba:dubbo:2.6.12")

    compileOnly("org.apache.ftpserver:ftpserver-core:1.2.1")

    compileOnly("org.apache.sshd:sshd-core:2.12.0")
    compileOnly("org.apache.sshd:sshd-sftp:2.12.0")

    testImplementation("junit:junit:4.13.1")
}

sourceSets["main"].java.srcDirs("src/main/gen")

intellijPlatform {
    buildSearchableOptions = false
    autoReload = false
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "230"
            untilBuild = provider { null }
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    runIde {
        autoReload = false
    }

    register<Copy>("copyDubboLibToSandbox") {
        dependsOn("prepareSandbox")
        from("dubboLib")
        include("**/*.jar")
        into(layout.buildDirectory.dir("idea-sandbox/IC-$ideaVersion/plugins/${project.name}/lib/dubboLib"))
    }

    register<Copy>("copyFtpLibToSandbox") {
        dependsOn("prepareSandbox")
        from("ftpLib")
        include("**/*.jar")
        into(layout.buildDirectory.dir("idea-sandbox/IC-$ideaVersion/plugins/${project.name}/lib/ftpLib"))
    }

    register<Copy>("copySftpLibToSandbox") {
        dependsOn("prepareSandbox")
        from("sftpLib")
        include("**/*.jar")
        into(layout.buildDirectory.dir("idea-sandbox/IC-$ideaVersion/plugins/${project.name}/lib/sftpLib"))
    }

    register<Delete>("deleteDubboLibOfSandbox") {
        delete(layout.buildDirectory.dir("idea-sandbox/IC-$ideaVersion/plugins/${project.name}/lib/dubboLib"))
    }

    register<Delete>("deleteFtpLibOfSandbox") {
        delete(layout.buildDirectory.dir("idea-sandbox/IC-$ideaVersion/plugins/${project.name}/lib/ftpLib"))
    }

    register<Delete>("deleteSftpLibOfSandbox") {
        delete(layout.buildDirectory.dir("idea-sandbox/IC-$ideaVersion/plugins/${project.name}/lib/sftpLib"))
    }

    named("runIde") {
        dependsOn("copyDubboLibToSandbox", "copyFtpLibToSandbox", "copySftpLibToSandbox")
    }

    named("buildPlugin") {
        dependsOn("deleteDubboLibOfSandbox", "deleteFtpLibOfSandbox", "deleteSftpLibOfSandbox")
    }

    jar {
        // kt文件不知道被哪个配置影响导致被编译了两次,所以这里暂时配置下
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}