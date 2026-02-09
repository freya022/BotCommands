plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(libs.ksp)
    implementation(libs.kotlinx.serialization)
    implementation(libs.jetbrains.markdown)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}
