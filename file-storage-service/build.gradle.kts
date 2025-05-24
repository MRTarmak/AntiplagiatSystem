plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}