import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("de.eldoria.plugin-yml.paper") version "0.7.1"

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
    paperLibrary("io.lettuce:lettuce-core:6.8.1.RELEASE")
    compileOnly("org.projectlombok:lombok:1.18.42")
    compileOnly("com.zaxxer:HikariCP:7.0.2")
    compileOnly("me.clip:placeholderapi:2.11.6")
    annotationProcessor("org.projectlombok:lombok:1.18.42")


}

paper {
    main = "it.einjojo.essentials.EssentialsPlugin"
    foliaSupported = false
    authors = listOf("EinJOJO")
    description = "Provides the economy- and basic commands, scoreboard, position management, playtime, etc."
    website = "https://einjojo.it"
    apiVersion = "1.20"
    loader = "it.einjojo.essentials.PluginLibrariesLoader"
    generateLibrariesJson = true
    serverDependencies {
        register("PlaceholderAPI") {
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
tasks.test {
    useJUnitPlatform()
}