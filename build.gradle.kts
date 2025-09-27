import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
    id("com.gradleup.shadow") version "9.2.2"

}

group = "it.einjojo"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    compileOnly("it.einjojo.playerapi:api:1.2.0")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    paperLibrary("com.zaxxer:HikariCP:7.0.2")
    paperLibrary("org.postgresql:postgresql:42.7.8")
    paperLibrary("org.flywaydb:flyway-database-postgresql:11.13.2")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    //cloudnet
    implementation(platform("eu.cloudnetservice.cloudnet:bom:4.0.0-RC14"))
    compileOnly("eu.cloudnetservice.cloudnet:driver-api")
    compileOnly("eu.cloudnetservice.cloudnet:wrapper-jvm-api")
    compileOnly("eu.cloudnetservice.cloudnet:bridge-api")

    // sucks when provided
    implementation("io.lettuce:lettuce-core:6.8.1.RELEASE")
    implementation("org.incendo:cloud-core:2.0.0")
    implementation("org.incendo:cloud-annotations:2.0.0")
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")


}

paper {
    main = "net.wandoria.essentials.EssentialsPlugin"
    foliaSupported = false
    authors = listOf("EinJOJO")
    description = "Provides the economy- and basic commands, scoreboard, position management, playtime, etc."
    website = "https://einjojo.it"
    apiVersion = "1.20"
    loader = "net.wandoria.essentials.PluginLibrariesLoader"
    generateLibrariesJson = true
    serverDependencies {
        register("PlaceholderAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("CloudNet-Bridge") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("PlayerApi") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
    }
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))

    withSourcesJar()
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
        downloadPlugins {
            hangar("PlaceholderAPI", "2.11.6")
        }
    }
    shadowJar {
        relocate("io.lettuce", "net.wandoria.essentials.libs.lettuce")
        relocate("org.incendo.cloud", "net.wandoria.essentials.libs.cloud")
        relocate("io.netty", "net.wandoria.essentials.libs.netty")
        //relocate("org.flywaydb", "net.wandoria.essentials.libs.flywaydb")
    }
}
tasks.test {
    useJUnitPlatform()
}