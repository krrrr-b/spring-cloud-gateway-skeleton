val projectGroup: String by project
val projectVersion: String by project
val springBootVersion: String by project
val springCoreVersion: String by project
val springCloudConfigVersion: String by project
val jacksonVersion: String by project
val resilience4jVersion: String by project
val jacocoVersion: String by project
val commonIoVersion: String by project
val javaxElVersion: String by project
val springCloudVersion: String by project
val javaxAnnotationApiVersion: String by project
val janinoVersion: String by project
val logbackVersion: String by project

plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("jvm")
    kotlin("plugin.spring") apply false
    kotlin("plugin.jpa") apply false

    id("org.jlleitschuh.gradle.ktlint") version "9.3.0"
}

java.sourceCompatibility = JavaVersion.VERSION_11

buildscript {
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.3.0")
    }
}

allprojects {
    group = projectGroup
    version = System.getenv("VERSION") ?: projectVersion

    repositories {
        mavenLocal()
        jcenter()

        maven {
            url = uri("https://oss.sonatype.org/content/groups/public")
        }

        maven {
            url = uri("http://oss.jfrog.org/artifactory/oss-snapshot-local")
        }
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }

        imports {
            mavenBom("org.springframework.cloud:spring-cloud-config:$springCloudConfigVersion")
        }

        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        }
    }

    dependencies {
        implementation(kotlin("reflect"))
        implementation(kotlin("stdlib-jdk8"))

        compileOnly("org.springframework.boot:spring-boot-configuration-processor")
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        implementation("org.springframework:spring-core:$springCoreVersion")
        api("org.springframework.boot:spring-boot-starter-parent:$springBootVersion")

        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        implementation("org.springframework.cloud:spring-cloud-starter-config")
        implementation("org.apache.commons:commons-lang3")
        implementation("org.apache.commons:commons-pool2")
        compileOnly("commons-io:commons-io:$commonIoVersion")

        implementation("org.codehaus.janino:janino:$janinoVersion")
        implementation("ch.qos.logback:logback-classic:$logbackVersion")
        implementation("ch.qos.logback:logback-core:$logbackVersion")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
}
