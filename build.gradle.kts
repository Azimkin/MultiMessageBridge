plugins {
    kotlin("jvm") version "2.0.20"
    //kotlin("plugin.serialization") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "top.azimkin"
version = "0.3"

fun getVersionWithBuildNumber(): String {
    val buildFile = File("buildnumber.txt")
    if (!buildFile.exists()) {
        buildFile.createNewFile()
        buildFile.writeText("1")
    }
    val buildNumber = kotlin.run {
        val i = buildFile.readText().toInt()
        buildFile.writeText((i + 1).toString())
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

    implementation("net.dv8tion:JDA:5.1.2") {
        exclude(module="opus-java")
    }
    implementation("me.scarsz.jdaappender:jda5:1.2.3") {
        exclude(group="net.dv8tion", module = "JDA")
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
        //exclude("com/fasterxml/**")
        //relocate("com.fasterxml", "top.azimkin.relocate.com.fasterxml")
        //relocate("com.yaml", "top.azimkin.relocate.com.yaml")

    }

    register("buildProd") {
        version = getVersionWithBuildNumber()
        dependsOn("shadowJar")
    }

    test {
        useJUnitPlatform()
    }
}
