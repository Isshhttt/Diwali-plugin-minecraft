plugins {
    `java`
}

group = "com.ishttt.diwali"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

val packageResourcePack = tasks.register<Zip>("packageResourcePack") {
    archiveFileName.set("DiwaliPack.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from("resourcepack")
}

tasks.processResources {
    from(packageResourcePack)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}
