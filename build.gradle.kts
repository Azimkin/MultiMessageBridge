import java.util.*

plugins {
    kotlin("jvm") version "2.3.20"
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("maven-publish")
}

group = "top.azimkin"
version = "0.5.2"

fun getVersionWithBuildNumber(): String {
    val buildFile = File("buildnumber.properties")
    if (!buildFile.exists()) {
        buildFile.createNewFile()
    }
    val properties = Properties().apply { load(buildFile.inputStream()) }
    val buildNumber = kotlin.run {
        var i = properties.getProperty(project.version.toString())?.toInt() ?: 0
        properties.setProperty(project.version.toString(), (++i).toString())
        buildFile.outputStream().use { properties.store(it, "Generated") }
        i
    }
    val currentVersion = version
    return "$currentVersion-b$buildNumber"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("com.github.pengrad:java-telegram-bot-api:9.6.0")
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.13")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("net.luckperms:api:5.5")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.github.TheJeterLP:ChatEx:v3.2.2")
    compileOnly("net.essentialsx:EssentialsX:2.20.0")
    compileOnly(fileTree("./libs") { include("*.jar") })
    compileOnly("com.discord4j:discord4j-core:3.3.2")
    implementation("me.scarsz.jdaappender:discord4j:1.2.4") {
        exclude(group = "discord4j", module = "discord4j")
    }


    // tests
    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
}

val targetJavaVersion = 17
kotlin {
    jvmToolchain(targetJavaVersion)
}

java {
    withJavadocJar()
}

tasks {
    runServer {
        minecraftVersion("1.21.11")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }

    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }

        exclude("kotlin/**")
//
//        exclude("org/**")
//        exclude("javax/**")
//        exclude("com/google/gson/**")
//        exclude("discord4j/**")
//        exclude("com/discord4j/**")
//        exclude("com/github/**")
//        exclude("com/fasterxml/**")
//        exclude("com/austinv11/**")
//        exclude("reactor/**")
//        exclude("io/netty/**")
//        exclude("google/**")
//        exclude("META-INF/**")
//
        relocate("io.netty", "remap.netty")
    }

    test {
        useJUnitPlatform()
    }

    register("publishRelease") {
        dependsOn("publish")
    }
}

fun isReleaseBuild(): Boolean =
    gradle.startParameter.taskNames.contains("publishRelease") ||
        project.findProperty("release") == "true"

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            val ghRepo = (project.findProperty("githubRepository") as String?)
                ?: System.getenv("GITHUB_REPOSITORY")
                ?: "Azimkin/MultiMessageBridge"
            url = uri("https://maven.pkg.github.com/$ghRepo")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                    ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN")
                    ?: project.findProperty("gpr.key") as String?
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            artifactId = "MultiMessageBridge"
            version =
                if (isReleaseBuild()) project.version.toString() else getVersionWithBuildNumber()
            from(components["java"])
            artifact(tasks.kotlinSourcesJar)
            pom {
                name.set("MultiMessageBridge")
                description.set("Cross-platform chat bridge for Minecraft, Discord and Telegram")
                url.set("https://github.com/Azimkin/MultiMessageBridge")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/Azimkin/MultiMessageBridge/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("azimkin")
                        name.set("Azimkin")
                    }
                }
                scm {
                    url.set("https://github.com/Azimkin/MultiMessageBridge")
                    connection.set("scm:git:git://github.com/Azimkin/MultiMessageBridge.git")
                    developerConnection.set("scm:git:ssh://github.com/Azimkin/MultiMessageBridge.git")
                }
            }
        }
    }
}
