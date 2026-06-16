rootProject.name = "class-list-generator"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("../reflection-metadata-commons") {
    dependencySubstitution {
        substitute(module("dev.freya02:reflection-metadata-commons")).using(project(":"))
    }
}
