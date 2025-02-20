import java.net.URI

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "org.javamaster"
version = "2.0.0"

repositories {
    maven { url = URI("https://maven.aliyun.com/nexus/content/groups/public/") }
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.mozilla:rhino:1.7.15")
    implementation("com.github.javafaker:javafaker:1.0.2")
    implementation("com.alibaba:dubbo:2.6.12")
    implementation("org.apache.curator:curator-client:4.0.1")
    implementation("org.apache.curator:curator-framework:4.0.1")
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
            "com.hxl.plugin.cool-request:2024.12.1",
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
        sinceBuild.set("231.*")
        untilBuild.set("243.*")
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
