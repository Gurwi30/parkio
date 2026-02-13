plugins {
    id("java")
    application
}

group = "it.parkio"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("it.parkio.app.ParkIO")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:3.7")
    implementation("org.jxmapviewer:jxmapviewer2:2.8")

    implementation("com.google.code.gson:gson:2.13.2")

    compileOnly("org.jetbrains:annotations:26.0.2-1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}