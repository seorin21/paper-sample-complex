import io.papermc.paperweight.tasks.RemapJar

plugins {
    alias(libs.plugins.paperweight) apply false
}

subprojects {
    apply(plugin = rootProject.libs.plugins.paperweight.get().pluginId)
    dependencies {
        implementation(projectApi)
        implementation(projectCore)

        val paperweight = (this as ExtensionAware).extensions.getByName("paperweight")
                as io.papermc.paperweight.userdev.PaperweightUserDependenciesExtension
        paperweight.paperDevBundle("${name.substring(1)}-R0.1-SNAPSHOT")
    }
}

coreDevJar {
    from(subprojects.map { it.sourceSets["main"].output })
}

coreReobfJar {
    subprojects.map { it.tasks.named("reobfJar").get() as RemapJar }.onEach {
        from(zipTree(it.outputJar))
    }.let {
        dependsOn(it)
    }
}

coreSourcesJar {
    from(subprojects.map { it.sourceSets["main"].allSource })
}