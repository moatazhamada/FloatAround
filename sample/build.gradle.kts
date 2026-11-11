plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.houseofalgorithms.floataround.sample"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.houseofalgorithms.floataround.sample"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":floataround"))
    implementation(platform("androidx.compose:compose-bom:2026.08.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.activity:activity-compose:1.13.0")
}
