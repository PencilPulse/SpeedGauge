plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

android {
    // ...existing code...
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/PencilPulse/SpeedGauge")
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            groupId = "com.pencilpulse"
            artifactId = "speedgauge"
            version = "1.0.0"
            
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}