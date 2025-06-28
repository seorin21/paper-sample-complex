plugins {
    alias(libs.plugins.publish)
    signing
}

publishing {
    repositories {
        mavenLocal()

        maven {
            name = "server"
            url = rootProject.uri(".server/libraries")
        }
    }

    publications {
        create<MavenPublication>("api") {
            artifactId = projectApi.name
            from(projectApi.components["java"])
            // 위에서 생성한 'sourcesJar', 'dokkaJar' 작업을 아티팩트로 추가합니다.
            artifact(projectApi.tasks.named("sourcesJar", Jar::class.java))
            artifact(projectApi.tasks.named("dokkaJar", Jar::class.java))
        }

        create<MavenPublication>("core") {
            artifactId = projectCore.name
            from(projectCore.components["java"])
            artifact(projectCore.tasks.named("sourcesJar", Jar::class.java))
            artifact(projectCore.tasks.named("dokkaJar", Jar::class.java))
            artifact(coreReobfJar)
        }
    }
}

mavenPublishing {
    pom {
        name.set(rootProject.name)
        description.set("유튜브 \"서린\"의 컨텐츠 서버 '잔혹한 추위'를 위한, 의존성 라이브러리입니다.")
        url.set("https://github.com/mulgyeolpyo/${rootProject.name}")

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
            connection.set("scm:git:git://github.com/mulgyeolpyo/${rootProject.name}.git")
            developerConnection.set("scm:git:ssh://github.com:mulgyeolpyo/${rootProject.name}.git")
            url.set("https://github.com/mulgyeolpyo/${rootProject.name}")
        }
    }

    publishToMavenCentral()
}

signing {
    isRequired = true
    sign(publishing.publications)
}

projectPlugin.tasks.named("clipJar") {
    dependsOn(tasks.named("publishApiPublicationToServerRepository"))
    dependsOn(tasks.named("publishCorePublicationToServerRepository"))
}
