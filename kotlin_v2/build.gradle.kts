import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.rabbitmq:amqp-client:2.8.2")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.slf4j:slf4j-simple:1.6.6")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.1")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.13")

    testRuntime("org.junit.platform:junit-platform-launcher:1.4.1")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.1")
    testRuntime("org.junit.vintage:junit-vintage-engine:5.4.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging { events("passed", "skipped", "failed") }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Wrapper> {
    gradleVersion = "5.3.1"
}
