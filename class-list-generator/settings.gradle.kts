rootProject.name = "class-list-generator"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("../classpath-scanner-commons") {
    dependencySubstitution {
        substitute(module("dev.freya02:classpath-scanner-commons")).using(project(":"))
    }
}
