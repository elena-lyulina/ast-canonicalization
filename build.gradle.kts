import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.dokka") version "1.4.0-rc"
}
group = "io.github.elena-lyulina.ast-canonicalization"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    implementation("com.github.gumtreediff", "client", "2.1.2")
    implementation("com.github.gumtreediff", "client.diff", "2.1.2")
    implementation("com.github.gumtreediff", "core", "2.1.2")
    implementation("commons-io", "commons-io", "2.5")

    testImplementation("com.github.gumtreediff", "gen.python", "2.1.2")
    testImplementation(kotlin("test-junit"))
    testImplementation("net.lingala.zip4j", "zip4j", "2.6.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    test {
        useJUnitPlatform()

        dependsOn("cleanTest")

        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}