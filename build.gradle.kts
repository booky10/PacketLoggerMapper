import java.io.ByteArrayOutputStream

plugins {
    id("fabric-loom") version "1.1-SNAPSHOT"
    id("maven-publish")
}

fun getGitCommit(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

version = "1.0.0+fabric.${getGitCommit()}"
group = "dev.booky"

repositories {
    maven("https://api.modrinth.com/maven/") {
        content {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.4")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:0.14.17")

    // needs to be manually put into the "run/mods" folder,
    // because if bundled dependencies not loading using this
    modCompileOnly("maven.modrinth:stackdeobf:1.3.2")
    modCompileOnly("maven.modrinth:packetlogger:1.4.0")
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = project.name.lowercase()
        from(components["java"])
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    jar {
        from("LICENSE") {
            rename { return@rename "${it}_packetloggermapper" }
        }
    }
}
