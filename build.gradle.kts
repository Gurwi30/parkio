plugins {
    id("java")
    id("application")
    id("com.gradleup.shadow") version "9.0.0-beta17"
}

group = "it.parkio"
version = "1.0.0"

application {
    mainClass.set("it.parkio.app.ParkIO")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:3.7")
    implementation("org.jxmapviewer:jxmapviewer2:2.8")
    implementation("com.github.weisj:jsvg:2.0.0")

    implementation("com.google.code.gson:gson:2.13.2")

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.slf4j:jul-to-slf4j:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.29")
    implementation("org.fusesource.jansi:jansi:2.4.1")

    compileOnly("org.jetbrains:annotations:26.0.2-1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    jar {
        enabled = false
    }

    assemble {
        dependsOn(shadowJar)
    }

    startScripts {
        dependsOn(shadowJar)
    }

    distZip {
        dependsOn(shadowJar)
    }

    distTar {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")

        exclude("javax/**")
        exclude("java/**")
    }

}

tasks.test {
    useJUnitPlatform()
}