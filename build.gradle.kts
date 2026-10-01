plugins {
    id("fabric-loom") version "1.14.1"
    id("maven-publish")
}

group = property("maven_group")!!
version = property("mod_version") as String + "+mc" + property("minecraft_version") as String
val baseName = property("archives_base_name") as String


repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Terraformers"
                url = uri("https://maven.terraformersmc.com/")
            }
        }
        filter {
            includeGroup("com.terraformersmc")
        }
    }
}

base {
    archivesName.set(property("mod_name") as String)
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/amecs.accesswidener")
}





dependencies {
    //to change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    include(implementation("com.moulberry:mixinconstraints:${(property("mcon_version"))}")!!)
    include(modImplementation("wtf.cheeze:platformlanguageloader-fabric:${property("pll_version")}")!!)

    modImplementation("com.terraformersmc:modmenu:${property("modmenu_version")}")
    modCompileOnly("maven.modrinth:controlling:A6W4m3vi")
    modCompileOnly("maven.modrinth:searchables:${property("searchables_version")}")
    if (providers.gradleProperty("withControlling").isPresent) {
        modRuntimeOnly("maven.modrinth:controlling:A6W4m3vi")
        modRuntimeOnly("maven.modrinth:searchables:${property("searchables_version")}")
    }
}


tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("support_range", project.property("support_range") as String)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version, "support_range" to project.property("support_range") as String))

    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}


java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}


tasks.named<Jar>("jar") {

    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}



val clientTest = sourceSets.create("clientTest") {
    java.setSrcDirs(listOf(rootProject.file("src/clientTest/java")))
    resources.setSrcDirs(listOf(rootProject.file("src/clientTest/resources")))
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath
}
loom {
    mods {
        create("amecs-controls-tests") { sourceSet(clientTest) }
    }
    runs {
        create("controlsTest") {
            client()
            source(clientTest)
            runDir("run-controls-test")
            vmArg("-Damecs.controlsTest=true")
        }
    }
}
