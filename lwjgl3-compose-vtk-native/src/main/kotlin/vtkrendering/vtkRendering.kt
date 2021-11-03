package vtkrendering

import vtk.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.math.cos
import kotlin.math.sin

private fun makeActor(src: vtkPolyDataAlgorithm, vararg color: Double = doubleArrayOf(0.0, 0.0, 0.0)): vtkActor {
    val actor = vtkActor()
    actor.GetProperty().SetColor(color)

    val mapper = vtkPolyDataMapper()
    src.Update()
    mapper.SetInputData(src.GetOutput())
    mapper.SetColorModeToDefault()

    actor.SetMapper(mapper)
    return actor
}

class RenderSurface(var width:Int, var height:Int) {
    fun setSize(w:Int, h:Int) {
        width = w
        height = h
    }
}

class VtkRenderCommands(val surface:RenderSurface) {

    companion object {
        private val cacheRoot = "${System.getProperty("user.home")}/.vtk/"

        init {
            println("library path is: ${System.getProperty("java.library.path")}")
            System.loadLibrary("jawt")
//            val jawt = System.getProperty("java.library.path").split(":").map {
//                File(it).listFiles { f->f.nameWithoutExtension.endsWith("jawt") }.firstOrNull()
//            }.firstOrNull()
//            jawt?.absolutePath?.let { System.load(it) } ?: throw RuntimeException("Can't locate jawt")
//            System.load("/home/wgryglas/Code/Java/graalvm-ce-java11-21.3.0/lib/libjawt.so")

            if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
                for (lib in vtkNativeLibrary.values()) {
                    if (!lib.IsLoaded()) {
                        println("${lib.GetLibraryName()} not loaded")
                    }
                }
            }

//            File(cacheRoot).apply {
//                if(!exists())
//                    mkdirs()
//            }
//            val libFiles = vtkNativeLibrary.values().filter { it.IsBuilt() }.map {
//                val libName = System.mapLibraryName(it.GetLibraryName())
//                val file = File(cacheRoot, libName)
//                if(!file.exists()) {
//                    //might fail if multiple instance would load lib, then need to write tmp file and make atomic move to target location
//                    VtkRenderCommands::class.java.getResourceAsStream(libName)?.use { input->
//                        Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
//                    } ?: throw LibraryLoadException("vtk library $libName not found in the ${VtkRenderCommands::class.simpleName} resource path")
//                }
//                file
//            }
//            libFiles.forEach { file->
//                System.load(file.absolutePath)
//            }

            // This line redirects warnings/errors to vtkError.txt
            vtkNativeLibrary.DisableOutputWindow(null)
        }
    }

    val renWin: vtkGenericOpenGLRenderWindow = vtkGenericOpenGLRenderWindow()
    val ren: vtkRenderer = vtkRenderer()

    val cam = ren.GetActiveCamera()

    var initilized = false
    fun preRenderInit() {
        if(initilized) return
        initilized = true

        renWin.SetIsDirect(1)
        renWin.SetSupportsOpenGL(1)
        renWin.SetIsCurrent(true)
        renWin.AddRenderer(ren)
        renWin.SetMapped(1)
        renWin.SetPosition(0, 0)
        renWin.OpenGLInit()

//        renWin.SetPolygonSmoothing(1)
//        renWin.SetLineSmoothing(1)
//        ren.SetUseFXAA(true)
//        ren.SetUseShadows(1)

        ren.SetBackground(0.95, 0.95, 0.85)
        ren.SetBackgroundAlpha(1.0)
        ren.AddActor(
            makeActor(
                vtkConeSource().apply {
                    SetAngle(Math.PI * 2)
                    SetCenter(0.0, 0.0, 0.0)
                    SetRadius(0.5)
                    SetHeight(1.0)
                    SetResolution(50)
                    SetCapping(0)
                },
                0.0, 1.0, 0.0
            ).
            apply {
                SetPosition(1.0, 0.0, 0.0)
            }
        )
        ren.AddActor(
            makeActor(
                vtkSphereSource().apply {
                    SetRadius(0.5)
                    SetPhiResolution(20)
                    SetThetaResolution(20)
                },
                1.0, 0.5, 0.25
            ).
            apply {
                SetPosition(-0.5, -0.5, 0.0)
            }
        )
    }

    private var currWidth = 0
    private var currHeight = 0

    private fun assertWindowsSize() {
        if(currWidth != surface.width || currHeight != surface.height) {
            currWidth  = surface.width
            currHeight = surface.height
            renWin.SetSize(currWidth, currHeight)
        }
    }

    fun invoke(progression: Float) {
        assertWindowsSize()

        val ang = 10 * Math.PI * 2 * progression
        cam.SetPosition(-5.0 * sin(ang), 0.0, -5.0 * cos(ang))
        cam.SetFocalPoint(0.0, 0.0, 0.0)

        renWin.SetIsCurrent(true)
        renWin.OpenGLInit()
        renWin.Render()
    }
}