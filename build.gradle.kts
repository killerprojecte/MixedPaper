plugins {
    java
    `maven-publish`

    // Nothing special about this, just keep it up to date
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false

    // In general, keep this version in sync with upstream. Sometimes a newer version than upstream might work, but an older version is extremely likely to break.
    id("io.papermc.paperweight.patcher") version "1.5.11"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content { onlyForConfigurations(configurations.paperclip.name) }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.8.10:fat") // Must be kept in sync with upstream
    decompiler("net.minecraftforge:forgeflower:2.0.627.2") // Must be kept in sync with upstream
    paperclip("io.papermc:paperclip:3.0.3") // You probably want this to be kept in sync with upstream
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 17
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://ci.emc.gs/nexus/content/groups/aikar/")
        maven("https://repo.aikar.co/content/groups/aikar")
        maven("https://repo.md-5.net/content/repositories/releases/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://jitpack.io")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

paperweight {
    serverProject = project(":mixedpaper-server")

    remapRepo.set("https://maven.fabricmc.net/")
    decompileRepo.set("https://files.minecraftforge.net/maven/")

    useStandardUpstream("Pufferfish") {
        url.set(github("pufferfish-gg", "Pufferfish"))
        ref.set(providers.gradleProperty("pufferfishRef"))

        withStandardPatcher {
            baseName("Pufferfish")

            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            apiOutputDir.set(layout.projectDirectory.dir("mixedpaper-api"))

            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
            serverOutputDir.set(layout.projectDirectory.dir("mixedpaper-server"))
        }

        patchTasks.register("generatedApi") {
            isBareDirectory = true
            upstreamDirPath = "paper-api-generator/generated"
            patchDir = layout.projectDirectory.dir("patches/generated-api")
            outputDir = layout.projectDirectory.dir("paper-api-generator/generated")
        }
    }
}