plugins {
    java
    `java-library`
    `maven-publish`
    signing
}

group = "com.hpfxd.configurate"
version = "1.0.0"
description = "YAML loader for Configurate implemented using eo-yaml."

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
                description.set(project.description)
                name.set(project.name)

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
            name = "ossrh"

            url = if (version.toString().endsWith("-SNAPSHOT")) {
                uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            } else {
                uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            }

            credentials {
                username = property("ossrhUsername") as String
                password = property("ossrhPassword") as String
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
    useGpgCmd()
}
