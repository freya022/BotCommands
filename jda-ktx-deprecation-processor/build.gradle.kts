plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
}

dependencies {
    implementation(libs.ksp)
    implementation(libs.ksp.aa)

    implementation(libs.jspecify)

    implementation(libs.classgraph)
    implementation(libs.kotlin.metadata)
    runtimeOnly(libs.jda.ktx)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}
