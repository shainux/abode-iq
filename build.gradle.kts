val ktorVer: String by project
val kotlinVer: String by project
val logbackVer: String by project
val kMongoVersion: String by project
val jSerialCommVer: String by project
val embeddedMongoVer: String by project

plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

group = "io.sh"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVer")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVer")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVer")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVer")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVer")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVer")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVer")

    implementation("com.fazecast:jSerialComm:$jSerialCommVer")                    // 2.9.3 for MacOS, 2.6.2 for RaspberryPi
    implementation("org.litote.kmongo:kmongo-coroutine-serialization:$kMongoVersion")

    implementation("io.ktor:ktor-server-netty-jvm:$ktorVer")
    implementation("ch.qos.logback:logback-classic:$logbackVer")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVer"){
        exclude(group = "junit", module = "junit")
    }
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:$embeddedMongoVer")
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
