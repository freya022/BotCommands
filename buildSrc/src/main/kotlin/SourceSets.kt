import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named

fun Project.registerSourceSet(name: String) {
    extensions.configure<SourceSetContainer>("sourceSets") {
        val mainSourceSet = named<SourceSet>("main").get()

        register(name) {
            compileClasspath += mainSourceSet.output
            runtimeClasspath += mainSourceSet.output
        }
    }

    configurations["${name}Api"].extendsFrom(configurations["api"])
    configurations["${name}Implementation"].extendsFrom(configurations["implementation"])
    configurations["${name}CompileOnly"].extendsFrom(configurations["compileOnly"])
    configurations["${name}RuntimeOnly"].extendsFrom(configurations["runtimeOnly"])
}
