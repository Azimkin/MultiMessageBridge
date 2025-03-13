import java.util.*

plugins {
    kotlin("jvm") version "1.9.10"
    //kotlin("plugin.serialization") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("maven-publish")
}

group = "top.azimkin"
version = "0.4"

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

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "codemc"
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://nexus.scarsz.me/content/repositories/releases/")
    maven("https://jitpack.io")

    maven("https://storehouse.okaeri.eu/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("com.github.pengrad:java-telegram-bot-api:7.9.1")
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.5")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly(fileTree("./libs") { include("*.jar") })

    implementation("net.dv8tion:JDA:5.1.2") {
        exclude(module = "opus-java")
    }
    implementation("me.scarsz.jdaappender:jda5:1.2.3") {
        exclude(group = "net.dv8tion", module = "JDA")
    }

    // tests
    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
}

val targetJavaVersion = 17
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    runServer {
        minecraftVersion("1.19.4")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        exclude("kotlin/**")
        exclude("org/intellij/**")
        exclude("org/jetbrains/**")
        exclude("org/slf4j/**")
        exclude("javax/**")
        exclude("com/google/gson/**")
    }

    test {
        useJUnitPlatform()
    }

    register("javadocJar", Jar::class.java) {
        from(javadoc)
        archiveClassifier.set("javadoc")
    }

    register("sourcesJar", Jar::class.java) {
        from(sourceSets.main.get().allSource)
        archiveClassifier.set("sources")
    }

    register("publishRelease") {
        dependsOn("publish")
    }
}

publishing {
    repositories {
        maven {
            if (gradle.startParameter.taskNames.contains("publishRelease")) {
                name = "azimkinRepoReleases"
                url = uri("https://repo.azimkin.top/releases")
            } else {
                name = "azimkinRepoSnapshots"
                url = uri("https://repo.azimkin.top/snapshots")
            }
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "MultiMessageBridge"
            version =
                if (gradle.startParameter.taskNames.contains("publishRelease")) project.version.toString() else getVersionWithBuildNumber()
            from(components["java"])
            artifact(tasks.kotlinSourcesJar)
            artifact(tasks["javadocJar"])
        }
    }
}
