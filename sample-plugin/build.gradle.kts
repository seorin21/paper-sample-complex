import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import java.util.*

dependencies {
    implementation(projectApi)
}

extra.apply {
    val pluginName = rootProject.name.split('-').joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }

    set("pluginName", pluginName)
    set("packageName", rootProject.name.replace("-", ""))
    set("kotlinVersion", libs.versions.kotlin.get())
    set("paperVersion", libs.versions.paper.get().split('.').take(2).joinToString(separator = ".").replace("-R0", "")) //.replace("-R0", "") << 1.x.x가 아닌 1.x 버전인 경우, R0이 포함될 수 있음.
    set("pluginCommands", "")
    set("pluginLibraries", "")

    val pluginCommands = LinkedHashSet<String>()
    val pluginCommandPath = "${project.projectDir.path}\\src\\main\\kotlin\\${project.group.toString().split(".").joinToString("\\")}\\${pluginName.lowercase()}\\commands"
    File(pluginCommandPath).walk().forEach {
        if (it.isFile) {
            var commandName = it.name.split("Command").take(1).joinToString().replaceFirstChar { char -> char.lowercase() }
            commandName = commandName.mapIndexed { index, char ->
                "${if (index > 0 && char.isUpperCase() && commandName[index - 1].isLowerCase()) "_" else ""}${char}"
            }.joinToString("")

            pluginCommands += commandName.lowercase()
            set("pluginCommands", pluginCommands.joinToString("\n  ") { "$it:\n    description: plugin.yml 자동 작성으로 등록된 커맨드입니다.\n    usage: /<command>" })
        }
    }

    val pluginLibraries = LinkedHashSet<String>()

    configurations.findByName("implementation")?.allDependencies?.forEach { dependency ->
        val group = dependency.group ?: error("group is null")
        var name = dependency.name ?: error("name is null")
        var version = dependency.version

        if (dependency !is ProjectDependency) {
            if (group == "org.jetbrains.kotlin" && version == null) {
                version = getKotlinPluginVersion()
            }

            requireNotNull(version) { "version is null" }
            require(version != "latest.release") { "version is latest.release" }

            pluginLibraries += "$group:$name:$version"
            set("pluginLibraries", pluginLibraries.joinToString("\n  ") { "- '$it'" })
        }
    }
}

tasks {
    processResources {
        filesMatching("*.yml") {
            expand(project.properties)
            expand(extra.properties)
        }
    }

    fun registerJar(
        classifier: String,
        bundleProject: Project? = null,
        bundleTask: TaskProvider<org.gradle.jvm.tasks.Jar>? = null
    ) = register<Jar>("${classifier}Jar") {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set(classifier)

        from(sourceSets["main"].output)

        if (bundleProject != null) from(bundleProject.sourceSets["main"].output)

        if (bundleTask != null) {
            bundleTask.let { bundleJar ->
                dependsOn(bundleJar)
                from(zipTree(bundleJar.get().archiveFile))
            }
            exclude("clip-plugin.yml")
            rename("bundle-plugin.yml", "plugin.yml")
        } else {
            exclude("bundle-plugin.yml")
            rename("clip-plugin.yml", "plugin.yml")
        }
    }.also { jar ->
        register<Copy>("test${classifier.replaceFirstChar { it.titlecase() }}Jar") {
            val plugins = rootProject.file(".server/plugins-$classifier")

            from(jar)
            into(plugins)
        }
    }

    registerJar("dev", projectApi, coreDevJar)
    registerJar("reobf", projectApi, coreReobfJar)
    registerJar("clip")
}
