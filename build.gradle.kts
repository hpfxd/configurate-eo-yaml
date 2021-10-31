plugins {
    java
    `java-library`
    `maven-publish`
    signing
}

group = "com.hpfxd.configurate"
version = "1.0.0"
description = "YAML format loader for Configurate implemented using eo-yaml."

repositories {
    mavenCentral()
}

dependencies {
    // configurate-core
    api("org.spongepowered:configurate-core:${project.property("configurateVersion")}")

    // eo-yaml
    implementation("com.amihaiemil.web:eo-yaml:6.0.0")

    // test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

// The following settings are for publishing the project to a repository.

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
            }

            pom {
                url.set("https://github.com/hpfxd/configurate-eo-yaml")

                developers {
                    developer {
                        id.set("hpfxd")
                        name.set("Nate")
                        email.set("me@hpfxd.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/hpfxd/configurate-eo-yaml.git")
                    developerConnection.set("scm:git:git://github.com/hpfxd/configurate-eo-yaml.git")
                    url.set("https://github.com/hpfxd/configurate-eo-yaml")
                }

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/mit-license.php")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "hpfxd-repo"

            url = uri("https://repo.hpfxd.com/releases/")

            credentials {
                username = property("repository.hpfxd.username") as String
                password = property("repository.hpfxd.password") as String
            }
        }
    }
}
