tasks.getByName("jar") {
    enabled = true
}

tasks.getByName("bootJar") {
    version = System.getenv("VERSION") ?: project.version
    enabled = true
}

val springDocOpenApiVersion: String by project
val junitJupiterVersion: String by project
val resilience4jVersion: String by project

dependencies {
    api(project(":core:common"))
    api(project(":core:common-api"))
    api(project(":client:unknown1-client"))
    api(project(":client:unknown2-client"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("io.github.resilience4j:resilience4j-reactor:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-spring-boot2:$resilience4jVersion")
    implementation("org.springdoc:springdoc-openapi-webflux-ui:$springDocOpenApiVersion")

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}
