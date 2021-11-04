import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    // __KOTLIN_COMPOSE_VERSION__
    kotlin("jvm") version "1.5.31"
    // __LATEST_COMPOSE_RELEASE_VERSION__
    id("org.jetbrains.compose") version (System.getenv("COMPOSE_TEMPLATE_COMPOSE_VERSION") ?: "1.0.0-beta3")
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

val example:String by project
val windowType = example.split("-")[0]
val commandsType = example.split("-")[1]

val isLwjgl  = windowType == "lwjgl3"
val isAwt    = windowType == "awt"
val isVtk    = commandsType == "vtk"
val isRawOGL = commandsType =="rawogl"

val vtkLibDir:String? by project
val vtkJarPath:String? by project
if(isVtk) {
    assert(vtkLibDir != null && vtkJarPath !=null) {
        "Using vtk rendering commands but vtkLibDir and vtkJarPath gradle properties are missing"
    }
}

val runClass:String =  when {
    isLwjgl -> "MainKt"
    isAwt -> "awt.MainKt"
    else-> throw RuntimeException("Unknown Window Type")
}
val renderCommandsClass:String = when {
    isVtk -> "VtkRenderCommands"
    isRawOGL -> "RawOGLCommands"
    else -> throw RuntimeException("Unknown rendering commands type")
}

val os = System.getProperty("os.name").let { name->
    when {
        name == "Mac OS X" -> "macos"
        name == "Linux" -> "linux"
        name.startsWith("Win") -> "windows"
        else -> throw Error("Unknown OS $name")
    }
}

if(!isVtk) {
    sourceSets {
        getByName("main").withConvention(KotlinSourceSet::class) {
            kotlin.exclude("**/VtkRenderCommands.kt")
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.lwjgl:lwjgl:3.2.3")
    implementation("org.lwjgl:lwjgl-glfw:3.2.3")
    implementation("org.lwjgl:lwjgl-opengl:3.2.3")
    implementation("org.lwjgl:lwjgl:3.2.3:natives-$os")
    implementation("org.lwjgl:lwjgl-glfw:3.2.3:natives-$os")
    implementation("org.lwjgl:lwjgl-opengl:3.2.3:natives-$os")

    implementation("org.lwjglx:lwjgl3-awt:0.1.8")

    if(isVtk && vtkJarPath != null) {
        implementation(files(vtkJarPath))
    }
}



compose.desktop {
    application {

        mainClass = runClass
        args.add(renderCommandsClass)
        args.add(commandsType)

        if(isVtk && vtkLibDir != null) {
            jvmArgs.add("-Djava.library.path=$vtkLibDir")
        }

//        nativeDistributions {
//            appResourcesRootDir.set(project.layout.projectDirectory.dir("xxx"))
//            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
//            packageName = "KotlinJvmComposeDesktopApplication"
//            packageVersion = "1.0.0"
//        }
    }
}