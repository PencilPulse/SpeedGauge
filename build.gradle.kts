// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("git@github.com:PencilPulse/SpeedGauge.git")
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/PencilPulse/SpeedGauge")
        }
    }
}