import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

dependencies {
    api(project(":data:common"))
    api(project(":core:common-api"))

    api("org.springframework.boot:spring-boot-starter-data-redis-reactive")
}
