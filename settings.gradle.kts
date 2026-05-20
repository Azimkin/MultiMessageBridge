import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.mavenCentral

rootProject.name = "MultiMessageBridge"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
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
        maven {
            name = "azimkinReleases"
            url = uri("https://repo.azimkin.dev/releases")
        }
        maven("https://jitpack.io")

        maven("https://storehouse.okaeri.eu/repository/maven-public/")
        maven("https://repo.essentialsx.net/releases/")
    }
}