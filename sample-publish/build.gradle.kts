plugins {
    `maven-publish`
    signing
}

projectPlugin.tasks.named("clipJar") {
    dependsOn(tasks.named("publishApiPublicationToServerRepository"))
    dependsOn(tasks.named("publishCorePublicationToServerRepository"))
}

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
                description.set("유튜브 \"서린\" 종합 프로젝트를 위한, 의존성 라이브러리입니다.")
                url.set("https://github.com/monun/${rootProject.name}")

                licenses {
                    license {
                        name.set("GNU General Public License version 3")
                        url.set("https://opensource.org/licenses/GPL-3.0")
                    }
                }

                developers {
                    developer {
                        id.set("seorin21")
                        name.set("서린")
                        email.set("seorin021@gmail.com")
                        url.set("https://github.com/seorin21")
                        roles.addAll("developer")
                        timezone.set("Asia/Seoul")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/seorin21/${rootProject.name}.git")
                    developerConnection.set("scm:git:ssh://github.com:seorin21/${rootProject.name}.git")
                    url.set("https://github.com/seorin21/${rootProject.name}")
                }
            }
        }

        create<MavenPublication>("api") {
            setup(projectApi)
        }

        create<MavenPublication>("core") {
            setup(projectCore)
            artifact(coreReobfJar)
        }

    }
}

signing {
    isRequired = true
    sign(publishing.publications["api"], publishing.publications["core"])
}