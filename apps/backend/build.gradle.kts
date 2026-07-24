import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.prestamosfacil"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:1.20.6")
    }
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql")

    // Flyway
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Caffeine (rate limiting + session cache)
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Mail + Thymeleaf (email notifications)
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // OpenAPI / Swagger (SpringDoc v3.x compatible with Spring Boot 4.x)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    // Jackson (JSON)
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.0")
    testImplementation("com.h2database:h2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "PACKAGE"
            excludes = listOf(
                "com.prestamosfacil",
                "com.prestamosfacil.infrastructure.configuration",
                "com.prestamosfacil.infrastructure.security.ratelimit",
                "com.prestamosfacil.infrastructure.adapter.out.persistence.postgres",
                "com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.*",
                "com.prestamosfacil.infrastructure.adapter.out.notification",
                "com.prestamosfacil.infrastructure.adapter.out.notification.*",
                "com.prestamosfacil.infrastructure.adapter.out.evaluation",
                "com.prestamosfacil.infrastructure.adapter.out.reporting",
                "com.prestamosfacil.infrastructure.adapter.out.storedprocedure",
                "com.prestamosfacil.infrastructure.shared.notification",
                "com.prestamosfacil.infrastructure.adapter.in.rest.auth",
                "com.prestamosfacil.infrastructure.adapter.in.rest.auth.mapper",
                "com.prestamosfacil.infrastructure.adapter.in.rest.auth.mapper.*",
                "com.prestamosfacil.infrastructure.adapter.in.rest.*",
                "com.prestamosfacil.application.loanapplication.command",
                "com.prestamosfacil.application.customer.command",
                "com.prestamosfacil.domain.loanapplication.port.out",
                "com.prestamosfacil.domain.customer.port.out",
                "com.prestamosfacil.domain.loan.port.out",
                "com.prestamosfacil.domain.loantype.port.out",
                "com.prestamosfacil.domain.notification.port.out",
                "com.prestamosfacil.domain.paymentplan.port.out"
            )
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.30".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.named("bootRun") {
    dependsOn(tasks.named("classes"))
}
