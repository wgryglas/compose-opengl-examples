import org.gradle.internal.os.OperatingSystem


plugins {
    application
    java
    kotlin("jvm") version "1.5.31"
    id("org.mikeneck.graalvm-native-image") version "v1.4.1"
    id("org.jetbrains.compose") version (System.getenv("COMPOSE_TEMPLATE_COMPOSE_VERSION") ?: "1.0.0-beta3")
}


group = "com.github.wgryglas"
description = "LWJGL3 HelloWorld demo with native image by GraalVM"
version = "0.0.1-SNAPSHOT"


val lwjglVersion = "3.2.3"
val graalvmVersion:String by project
val graalvmPath:String? by project
val vtkJarPath:String by project
val vtkSharedLibPath:String by project

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

    implementation(files(vtkJarPath))

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

application {
    applicationDefaultJvmArgs = listOf(
        "-Djava.library.path=$vtkSharedLibPath:/usr/lib/jvm/java-11-openjdk-amd64/lib"
    )
}

fun getGraalVmHome() =
    System.getenv("GRAALVM_HOME") ?: graalvmPath ?: throw Exception("GRAALVM path is not specified as GRAALVM_HOME env variable or in gradle.properites file")

nativeImage {
//    mainClass = mainClassPathName
    graalVmHome = getGraalVmHome()
    buildType { build ->
        build.executable {
            main = mainClassPathName
        }
    }
    executableName = project.name
    outputDirectory = file("$buildDir/$nativeImageDirName")

    arguments(
        "-J-Xmx4G",
        "--no-fallback",
        "--initialize-at-run-time=org.lwjgl,java.awt,androidx.compose,vtkrendering",
        "--native-image-info",
        "--verbose",
        "--report-unsupported-elements-at-runtime",
        "-esa",
        //"-H:+TraceNativeToolUsage",
        "-Djava.awt.headless=false",
        "-Dvtk.library.path=$vtkSharedLibPath",
        "-H:TempDirectory=$buildDir/tmp/$nativeImageDirName",
        "-H:IncludeResources=\"$vtkSharedLibPath/libvtk.*\\.so.*\"",
        "-J-Dgraal.LogFile=$buildDir/$nativeImageDirName/build-log.log"
    )

    // if (currentPlatform == "windows") {
    //     finalizedBy removeUnneededDllFiles
    // }
    
    // On windows change subsystem after generating native executable:
    //
    // EDITBIN /SUBSYSTEM:WINDOWS <executable.exe>
    //
    // details: https://github.com/oracle/graal/issues/2256
}

//javaexec { mainClass.set(mainClassPathName) }

generateNativeImageConfig {
    enabled = false
    graalVmHome = getGraalVmHome()
    byRunningApplicationWithoutArguments()
}

//tasks.withType<org.mikeneck.graalvm.DefaultGenerateNativeImageConfigTask> {
//    mainClass.set(mainClassPathName)
//}

