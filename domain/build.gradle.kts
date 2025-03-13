import org.gradle.kotlin.dsl.kotlin

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("com.google.devtools.ksp")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {

    // Flow
    implementation(libs.kotlinx.coroutines.core)

    // Hilt
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    // Resultat - Kotlin Result with loading state support
//    implementation(libs.resultat)

    // davidepanidev
    implementation(libs.kotlin.extensions)

    // Arbor
    implementation(libs.arbor.jvm)

}