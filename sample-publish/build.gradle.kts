plugins {
    `maven-publish`
    signing
}

project(":${rootProject.name}-plugin").tasks.named("clipJar") {
    dependsOn(tasks.named("publishApiPublicationToServerRepository"))
    dependsOn(tasks.named("publishCorePublicationToServerRepository"))
}

val projectAPI = project(":${rootProject.name}-api")
val projectCORE = project(":${rootProject.name}-core")

publishing {
    repositories {
        mavenLocal()

        maven {
            name = "server"
            url = rootProject.uri(".server/libraries")
        }

        maven {
            name = "central"

            credentials.runCatching {
                val nexusUsername: String by project
                val nexusPassword: String by project
                username = nexusUsername
                password = nexusPassword
            }.onFailure {
                logger.warn("Failed to load nexus credentials, Check the gradle.properties")
            }

            url = uri(
                if ("SNAPSHOT" in version as String) {
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                } else {
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                }
            )
        }
    }

    publications {
        fun MavenPublication.setup(target: Project) {
            artifactId = target.name
            from(target.components["java"])
            artifact(target.tasks["sourcesJar"])
            artifact(target.tasks["dokkaJar"])

            pom {
                name.set(target.name)
                url.set("https://github.com/monun/${rootProject.name}")

                licenses {
                    license {
                        name.set("GNU General Public License version 3")
                        url.set("https://opensource.org/licenses/GPL-3.0")
                    }
                }

                developers {
                    developer {
                        id.set("monun")
                        name.set("Monun")
                        email.set("monun1010@gmail.com")
                        url.set("https://github.com/monun")
                        roles.addAll("developer")
                        timezone.set("Asia/Seoul")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/monun/${rootProject.name}.git")
                    developerConnection.set("scm:git:ssh://github.com:monun/${rootProject.name}.git")
                    url.set("https://github.com/monun/${rootProject.name}")
                }
            }
        }

        create<MavenPublication>("api") {
            setup(projectAPI)
        }

        create<MavenPublication>("core") {
            setup(projectCORE)
            artifact(projectCORE.tasks.named("coreReobfJar"))
        }
    }
}

signing {
    isRequired = true
    sign(publishing.publications["api"], publishing.publications["core"])
}
