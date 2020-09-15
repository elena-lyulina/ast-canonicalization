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
    implementation("org.apache.commons", "commons-lang3", "3.11")


    testImplementation("com.github.gumtreediff", "gen.python", "2.1.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.0.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
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
//      Add -Pac.p3.path=/path/to/python3 to the command line to make it work
//      Todo: is there a better way?
        systemProperties = project.properties.filterKeys { it in listOf("ac.p3.path") }
    }
}