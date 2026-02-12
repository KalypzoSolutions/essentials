import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("de.eldoria.plugin-yml.paper") version "0.8.0"
    id("com.gradleup.shadow") version "9.3.1"
    id("me.qoomon.git-versioning") version "6.4.4"

}

group = "it.einjojo"
// set version
version = "0.0.0-SNAPSHOT"
gitVersioning.apply {
    refs {
        tag("v(?<tagVersion>[0-9].*)") {
            version = "\${ref.tagVersion}"
        }
        branch("main") {
            version = "\${describe.tag.version}-\${describe.distance}"
        }
        branch(".+") {
            version = "\${ref}-\${commit.short}"
        }
    }
    rev {
        version = "\${commit.short}"
    }
}

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "labymod"
        url = uri("https://dist.labymod.net/api/v1/maven/release/")
    }
    maven("https://repo.einjojo.it/releases")
    // maven("https://maven.pvphub.me/tofaa") not used
}

dependencies {
    compileOnly("net.labymod.serverapi:server-bukkit:1.0.9")

    compileOnly("it.einjojo.playerapi:api:1.5")
    compileOnly("it.einjojo:economy:2.0.1")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("net.luckperms:api:5.5")


    paperLibrary("com.zaxxer:HikariCP:7.0.2")
    paperLibrary("org.postgresql:postgresql:42.7.9")
    paperLibrary("org.flywaydb:flyway-database-postgresql:12.0.1")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    // head preview
    // paperLibrary("io.github.tofaa2:spigot:3.0.3-SNAPSHOT") not used yet. TODO: add packet events dep.
    // compileOnly("com.github.retrooper:packetevents-spigot:2.11.1")

    //cloudnet
    implementation(platform("eu.cloudnetservice.cloudnet:bom:4.0.0-RC17-SNAPSHOT"))
    compileOnly("eu.cloudnetservice.cloudnet:driver-api")
    compileOnly("eu.cloudnetservice.cloudnet:wrapper-jvm-api")
    compileOnly("eu.cloudnetservice.cloudnet:bridge-api")

    // sucks when provided
    implementation("io.lettuce:lettuce-core:7.4.0.RELEASE")
    paperLibrary("org.incendo:cloud-core:2.0.0")
    paperLibrary("org.incendo:cloud-annotations:2.0.0")
    annotationProcessor("org.incendo:cloud-annotations:2.0.0")
    paperLibrary("org.incendo:cloud-paper:2.0.0-beta.14")


}

paper {
    name = "EssentialsK"
    main = "de.kalypzo.essentials.EssentialsPlugin"
    foliaSupported = false
    authors = listOf("EinJOJO", "CrayonGamerHD")
    description = "Provides the economy- and basic commands, scoreboard, position management, etc."
    website = "https://einjojo.it"
    apiVersion = "1.20"
    loader = "de.kalypzo.essentials.PluginLibrariesLoader"
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
        register("LuckPerms") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("PlayerApi") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
        register("EconomyProviderPlugin") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
        register("LabyModServerAPI") {
            required = false
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
            modrinth("luckperms", "OrIs0S6b")
            url("https://github.com/wandoriamc/economy-provider/releases/download/v1.3.2/EconomyProviderPlugin-1.3.2-all.jar")  // economy provider 3.0.1
            url("https://github.com/wandoriamc/player-service-api/releases/download/v1.5.2/playerapi-paper-1.5.2.jar")
        }
    }
    shadowJar {
        relocate("io.lettuce", "net.wandoria.essentials.libs.lettuce")
        relocate("io.netty", "net.wandoria.essentials.libs.netty")
        archiveFileName.set("Essentials.jar")
    }

    generatePaperPluginDescription {
        useGoogleMavenCentralProxy()
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
