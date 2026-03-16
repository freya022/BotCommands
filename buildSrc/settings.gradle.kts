rootProject.name = "buildSrc"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("../spring-configuration-metadata-generator") {
    dependencySubstitution {
        substitute(module("dev.freya02:spring-configuration-metadata-generator")).using(project(":"))
    }
}
