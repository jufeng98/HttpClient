@file:Suppress("VulnerableLibrariesLocal")

import java.net.URI

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "org.javamaster"
version = "3.7.0"

repositories {
    maven { url = URI("https://maven.aliyun.com/nexus/content/groups/public/") }
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.mozilla:rhino:1.7.15")
    implementation("com.github.javafaker:javafaker:1.0.2")
    implementation("com.jayway.jsonpath:json-path:2.9.0")

    implementation("com.alibaba:dubbo:2.6.12") {
        exclude(group = "org.springframework", module = "spring-context")
        exclude(group = "org.javassist", module = "javassist")
        exclude(group = "org.jboss.netty", module = "netty")
    }

    testImplementation("junit:junit:4.13.1")
}

sourceSets["main"].java.srcDirs("src/main/gen")

intellij {
    version.set("2024.3")
    type.set("IC")
    plugins.set(
        listOf(
            "tasks",
            "com.intellij.java",
            "com.intellij.modules.json",
            "ris58h.webcalm:0.11.1",
        )
    )
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    jar {
        // kt文件不知道被哪个配置影响导致被编译了两次,所以这里暂时配置下
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("243.*")
    }

    runIde {
        autoReloadPlugins = false
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
