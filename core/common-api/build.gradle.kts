import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

val nettyVersion: String by project
val nettyReactorCoreVersion: String by project
val reactorKotlinExtensionsVersion: String by project
val nettyReactorVersion: String by project

dependencies {
    api(project(":core:common"))

    api("io.netty:netty-all:$nettyVersion")
    api("io.netty:netty-buffer:$nettyVersion")
    api("io.netty:netty-transport-native-epoll:$nettyVersion:linux-x86_64")

    api("io.projectreactor:reactor-core:$nettyReactorCoreVersion")
    api("io.projectreactor.addons:reactor-extra")
    api("io.projectreactor.kotlin:reactor-kotlin-extensions:$reactorKotlinExtensionsVersion")
    api("io.projectreactor.netty:reactor-netty:$nettyReactorVersion")

    api("org.springframework.boot:spring-boot-starter-aop")
    api("org.springframework.boot:spring-boot-starter-webflux")

    api("org.codehaus.janino:janino")
}
