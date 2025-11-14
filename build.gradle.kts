import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
    id("com.gradleup.shadow") version "9.2.2"

}

group = "it.einjojo"
version = "1.1.0-DEV"

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
    compileOnly("it.einjojo:economy:2.0.1")
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
    paperLibrary("org.incendo:cloud-core:2.0.0")
    paperLibrary("org.incendo:cloud-annotations:2.0.0")
    annotationProcessor("org.incendo:cloud-annotations:2.0.0")
    paperLibrary("org.incendo:cloud-paper:2.0.0-beta.10")


}

paper {
    name = "EssentialsW"
    main = "net.wandoria.essentials.EssentialsPlugin"
    foliaSupported = false
    authors = listOf("EinJOJO")
    description = "Provides the economy- and basic commands, scoreboard, position management, etc."
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
        register("EconomyProviderPlugin") {
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
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.add("-parameters")
    }
    runServer {
        environment("INTERNAL_SERVER_NAME", "run")
        minecraftVersion("1.21.4")
        downloadPlugins {
            hangar("PlaceholderAPI", "2.11.6")
            url("https://cloud.einjojo.it/s/YK8WMIJgrPIycnH/download")  // economy provider 3.0.1
            url("https://github.com/wandoriamc/player-service-api/releases/download/v1.4.3/playerapi-paper-1.4.3.jar")
        }
    }
    shadowJar {
        relocate("io.lettuce", "net.wandoria.essentials.libs.lettuce")
        relocate("io.netty", "net.wandoria.essentials.libs.netty")
        archiveFileName.set("Essentials.jar")
    }
}
tasks.test {
    useJUnitPlatform()
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
}
