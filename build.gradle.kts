plugins {
    id("java")
    id("jacoco")
}

group = "com.github.sol239.javafi"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of("23"))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.mockito:mockito-core:5.+")
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
    implementation("com.google.auto.service:auto-service-annotations:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    runtimeOnly("com.h2database:h2:2.2.224")
}

tasks.register<JavaExec>("server") {
    group = "application"
    description = "Run ServerParallel main class"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.github.sol239.javafi.server.ServerParallel")
}

tasks.register<JavaExec>("client") {
    group = "application"
    description = "Run Client main class"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.github.sol239.javafi.client.Client")
    standardInput = System.`in`
}


tasks.test {
    useJUnitPlatform {
        // Exclude @Tag("local-only") when running in CI
        if (System.getenv("CI") == "true") {
            excludeTags("local-only")
        }
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/coverage"))
    }

    val mainSourceSet = sourceSets.main.get()
    sourceDirectories.setFrom(mainSourceSet.allSource.srcDirs)
    classDirectories.setFrom(mainSourceSet.output.classesDirs)
    executionData.setFrom(fileTree(buildDir).include("jacoco/test.exec"))
}
