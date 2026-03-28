plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.kb9ut.pror"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kb9ut.pror"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Only keep English and Japanese resources (removes ~80 unused locale translations from dependencies)
        resourceConfigurations += listOf("en", "ja")

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Vico Charts
    implementation(libs.vico.compose.m3)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.room.testing)
}

// ── ktlint configuration ──
ktlint {
    android.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(true) // Don't break the build on style issues
    filter {
        exclude("**/generated/**")
    }
}

// ── detekt configuration ──
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/detekt-config.yml")
    parallel = true
    ignoreFailures = true // Don't break the build on analysis issues
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
}

// Wire ktlintCheck and detekt into the compileDebugKotlin task
tasks.matching { it.name == "compileDebugKotlin" }.configureEach {
    finalizedBy("ktlintCheck", "detekt")
}
