import org.gradle.internal.os.OperatingSystem


plugins {
    application
    java
    kotlin("jvm") version "1.5.31"
    id("org.graalvm.buildtools.native") version "0.9.4"
    id("org.jetbrains.compose") version (System.getenv("COMPOSE_TEMPLATE_COMPOSE_VERSION") ?: "1.0.0-beta3")
}


group = "com.github.wgryglas"
description = "LWJGL3 HelloWorld demo with native image by GraalVM"
version = "0.0.1-SNAPSHOT"


val lwjglVersion = "3.2.3"
val graalvmVersion:String by project
val graalvmPath:String? by project

println(graalvmVersion)

val mainClassPathName = "MainKt"

//detect the OS (assuming 64-bit, on Intel/AMD hardware)
val currentPlatform = with(OperatingSystem.current()) {
    when {
        isWindows -> "windows"
        isLinux -> "linux"
        isMacOsX -> "macos"
        else -> "unknown"
    }
}
val nativeImageDirName = "native-image-$currentPlatform"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}


dependencies {
    //add desktop compose dependency
    implementation(compose.desktop.currentOs)
}


application {
    mainClass.set(mainClassPathName)
    applicationName = project.name  //name of the resulting native executable
}

tasks.withType<JavaCompile> {
    options.release.set(11)
    options.encoding = "UTF-8"
}

nativeBuild {
    imageName.set(project.name)
    mainClass.set(mainClassPathName)
    debug.set(false)
    verbose.set(true)
    fallback.set(false)
	sharedLibrary.set(false)

    buildArgs.add("--initialize-at-run-time=org.lwjgl,java.awt,androidx.compose")
    buildArgs.add("--report-unsupported-elements-at-runtime")
    buildArgs.add("--allow-incomplete-classpath")
	buildArgs.add("-H:NativeLinkerOption=prefs.lib")

//    agent.set(true)
    useFatJar.set(true)
}
