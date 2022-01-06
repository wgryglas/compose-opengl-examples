import org.gradle.internal.os.OperatingSystem


plugins {
    application
    java
    kotlin("jvm") version "1.5.31"
 //   id("org.mikeneck.graalvm-native-image") version "v1.4.1"
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

//LWJGL modules used: minimal OpenGL
val lwjglModules = listOf("lwjgl", "lwjgl-glfw", "lwjgl-opengl")

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
    //get recommended dependency versions from the LWJGL BOM
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    //add desktop compose dependency
    implementation(compose.desktop.currentOs)

    //add LWJGL modules and the current OS's natives to the compile and runtime classpaths
    lwjglModules.forEach {
        implementation("org.lwjgl:$it")
        if (it != "lwjgl-egl")  //lwjgl-egl has no native libraries
            runtimeOnly("org.lwjgl:$it::natives-$currentPlatform")
    }

    //for compiling GraalVM substitution classes
    compileOnly("org.graalvm.nativeimage:svm:$graalvmVersion")
}



application {
    mainClass.set(mainClassPathName)
    applicationName = project.name  //name of the resulting native executable
}

tasks.withType<JavaCompile> {
    options.release.set(11)
    options.encoding = "UTF-8"
}
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//
//}

//compileJava {
//    options.release = 11  //use JDK11+ for compiling & running
//    options.encoding = "UTF-8"
//}

//run {
//    //get system properties specified from the command line (for debugging, etc.)
//    //and pass them on to the running application's JVM
//    systemProperties = System.getProperties()
//
//    //use the following jvmArgs for as many different run scenarios as possible,
//    //and for all the code-execution paths as much as possible,
//    //to generate (or merge with) the GraalVM native-image configuration files
//    //in the src/main/resources/META-INF/native-image directory.
//    //This directory is read by GraalVM during the native-image build.
//
//    jvmArgs = listOf("-agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image")
//}

//remove unneeded .dll files
// task removeUnneededDllFiles {
//     doLast {
//         ant.move(todir: "$buildDir/tmp/$nativeImageDirName") {
//             fileset(dir: "$buildDir/$nativeImageDirName") {
//                 include name: '*.dll'
//                 include name: '*.txt'
//             }
//         }
//     }
// }

fun getGraalVmHome() =
    System.getenv("GRAALVM_HOME") ?: graalvmPath ?: throw Exception("GRAALVM path is not specified as GRAALVM_HOME env variable or in gradle.properites file")

//nativeImage {
//    graalVmHome = getGraalVmHome()
//    buildType { build ->
//        build.executable {
//            main = mainClassPathName
//        }
//    }
//    executableName = project.name
//    outputDirectory = file("$buildDir/$nativeImageDirName")
//
//    arguments(
//        "--no-fallback",
//        "--initialize-at-run-time=org.lwjgl,java.awt,androidx.compose",
//        "--native-image-info",
//        "--verbose",
//        "--report-unsupported-elements-at-runtime",
//        //"-H:+TraceNativeToolUsage",
//        "-Djava.awt.headless=false",
//        "-H:TempDirectory=$buildDir/tmp/$nativeImageDirName"
//    )
//
//    // if (currentPlatform == "windows") {
//    //     finalizedBy removeUnneededDllFiles
//    // }
//
//    // On windows change subsystem after generating native executable:
//    //
//    // EDITBIN /SUBSYSTEM:WINDOWS <executable.exe>
//    //
//    // details: https://github.com/oracle/graal/issues/2256
//}
//
//generateNativeImageConfig {
//    enabled = false
//    graalVmHome = getGraalVmHome()
//    byRunningApplicationWithoutArguments()
//}


nativeBuild {
    imageName.set("compose-native")
    mainClass.set(mainClassPathName)
    debug.set(false)
    verbose.set(true)
    fallback.set(false)
	sharedLibrary.set(false)

//    configurationFileDirectories.setFrom(file(projectDir, "src/"))
    buildArgs.add("--initialize-at-run-time=org.lwjgl,java.awt,androidx.compose")
    buildArgs.add("--report-unsupported-elements-at-runtime")
//    buildArgs.add("H:TempDirectory=$buildDir/tmp/$nativeImageDirName")
    buildArgs.add("--allow-incomplete-classpath")
	buildArgs.add("-H:NativeLinkerOption=prefs.lib")

//   runtimeArgs.add("-Djava.awt.headless=false")

    agent.set(true)
    useFatJar.set(true)
}
