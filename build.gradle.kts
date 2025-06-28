plugins {
    idea
    alias(libs.plugins.kotlin)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish) apply false
}

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    apply(
        plugin =
            rootProject.libs.plugins.kotlin
                .get()
                .pluginId,
    )

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly(rootProject.libs.paper)
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

listOf(projectApi, projectCore).forEach { module ->
    with(module) {
        apply(
            plugin =
                rootProject.libs.plugins.dokka
                    .get()
                    .pluginId,
        )

        tasks {
            create<Jar>("sourcesJar") {
                archiveClassifier.set("sources")
                from(sourceSets["main"].allSource)
            }

            create<Jar>("dokkaJar") {
                archiveClassifier.set("javadoc")
                dependsOn("dokkaHtml")

                from(layout.buildDirectory.dir("dokka/html")) {
                    include("**")
                }
            }
        }
    }
}

idea {
    module {
        excludeDirs.add(file(".server"))
        excludeDirs.addAll(
            allprojects.map {
                it.layout.buildDirectory
                    .get()
                    .asFile
            },
        )
        excludeDirs.addAll(allprojects.map { it.file(".gradle") })
    }
}
