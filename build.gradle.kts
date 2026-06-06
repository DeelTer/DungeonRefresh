plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.4.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "ru.deelter"
version = "1.0.1"
description = "DungeonLootRefresher"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    implementation("org.bstats:bstats-bukkit:3.2.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    jar { enabled = false }
    shadowJar {
        archiveAppendix.set("")
        relocate("com.github.benmanes.caffeine", "${project.group}.shaded.caffeine")
        relocate("org.bstats", project.group.toString())
    }
    assemble { dependsOn(shadowJar) }
    runServer {
        minecraftVersion("26.1.2")
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        val props = mapOf("version" to version, "description" to description)
        filesMatching("plugin.yml") { expand(props) }
    }
}