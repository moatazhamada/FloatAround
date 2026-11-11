plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    `maven-publish`
}

android {
    namespace = "com.houseofalgorithms.floataround"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    publishing {
        singleVariant("release") { withSourcesJar() }
    }
}

dependencies {
    api(platform("androidx.compose:compose-bom:2026.08.00"))
    api("androidx.compose.foundation:foundation")
    testImplementation("junit:junit:4.13.2")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.moatazhamada"
            artifactId = "floataround"
            version = "0.2.0"
            afterEvaluate { from(components["release"]) }
        }
    }
}
