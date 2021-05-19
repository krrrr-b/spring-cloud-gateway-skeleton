import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

val jakartaVersion: String by project
val mongoDbEncryptVersion: String by project
val mongoDbAsyncDriverVersion: String by project
val mongoReactiveStreamsVersion: String by project
val springDataMongoDbVersion: String by project
val springDataMongoDbReactiveVersion: String by project
val jodaTimeVersion: String by project

dependencies {
    api(project(":data:common"))

    api("org.mongodb:mongodb-driver-async:$mongoDbAsyncDriverVersion")
    api("jakarta.platform:jakarta.jakartaee-api:$jakartaVersion")
    api("org.springframework.data:spring-data-mongodb:$springDataMongoDbVersion")
    api("org.springframework.boot:spring-boot-starter-data-mongodb-reactive:$springDataMongoDbReactiveVersion")
    api("org.mongodb:mongodb-driver-reactivestreams:$mongoReactiveStreamsVersion")
    api("com.bol:spring-data-mongodb-encrypt:$mongoDbEncryptVersion")
    api("joda-time:joda-time:$jodaTimeVersion")
    api("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
}
