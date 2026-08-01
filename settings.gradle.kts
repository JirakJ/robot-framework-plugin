pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

// Resolves the JDK 17 toolchain on machines that do not have one installed.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "robot-framework-plugin"
