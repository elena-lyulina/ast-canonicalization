import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
}
group = "io.github.elena-lyulina.ast-canonicalization"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    implementation("com.github.gumtreediff", "core", "2.1.2")
    implementation("com.github.gumtreediff", "client", "2.1.2")
    implementation("com.github.gumtreediff", "client.diff", "2.1.2")
    implementation("com.github.gumtreediff", "core", "2.1.2")
    implementation("com.github.gumtreediff", "gen.python", "2.1.2")
    implementation("com.github.gumtreediff", "gen.antlr3", "2.1.2")
    implementation("com.github.gumtreediff", "gen.antlr3", "2.1.2")
    implementation("com.github.gumtreediff", "gen.antlr3-antlr", "2.1.2")
    implementation("com.github.gumtreediff", "gen.antlr3-xml", "2.1.2")
    implementation("com.github.gumtreediff", "gen.antlr4", "2.1.2")
    implementation("com.github.gumtreediff", "gen.jdt", "2.1.2")
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
    }
}