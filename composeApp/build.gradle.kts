import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
        force("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
    }
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation("io.data2viz.d2v:d2v-axis:0.10.7")
            implementation("io.data2viz.d2v:d2v-chord:0.10.7")
            implementation("io.data2viz.d2v:d2v-color:0.10.7")
            implementation("io.data2viz.d2v:d2v-contour:0.10.7")
            implementation("io.data2viz.d2v:d2v-delaunay:0.10.7")
            implementation("io.data2viz.d2v:d2v-dsv:0.10.7")
            implementation("io.data2viz.d2v:d2v-ease:0.10.7")
            implementation("io.data2viz.d2v:d2v-force:0.10.7")
            implementation("io.data2viz.d2v:d2v-format:0.10.7")
            implementation("io.data2viz.d2v:d2v-geo:0.10.7")
            implementation("io.data2viz.d2v:d2v-hexbin:0.10.7")
            implementation("io.data2viz.d2v:d2v-hierarchy:0.10.7")
            implementation("io.data2viz.d2v:d2v-quadtree:0.10.7")
            implementation("io.data2viz.d2v:d2v-random:0.10.7")
            implementation("io.data2viz.d2v:d2v-scale:0.10.7")
            implementation("io.data2viz.d2v:d2v-shape:0.10.7")
            implementation("io.data2viz.d2v:d2v-tile:0.10.7")
            implementation("io.data2viz.d2v:d2v-time:0.10.7")
            implementation("io.data2viz.d2v:d2v-timer:0.10.7")
            implementation("io.data2viz.d2v:d2v-viz:0.10.7")

            
        }
    }
}

android {
    namespace = "mine.babbira.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "mine.babbira.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

