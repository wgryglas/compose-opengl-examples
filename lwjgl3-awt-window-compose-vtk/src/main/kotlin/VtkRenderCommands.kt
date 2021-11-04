import vtk.*
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

class VtkRenderCommands : RenderCommands {
    companion object {
        init {
            System.loadLibrary("jawt")
            if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
                for (lib in vtkNativeLibrary.values()) {
                    if (!lib.IsLoaded()) {
                        println("${lib.GetLibraryName()} not loaded")
                    }
                }
            }
            // This line redirects warnings/errors to vtkError.txt
            vtkNativeLibrary.DisableOutputWindow(null)

//            RenderCommands.register("vtk", VtkRenderCommands())
        }
    }

    val renWin: vtkGenericOpenGLRenderWindow = vtkGenericOpenGLRenderWindow()
    val ren: vtkRenderer = vtkRenderer()

    val cam = ren.GetActiveCamera()

    var initilized = false
    override fun prerenderInit(surface: RenderSurface) {
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

    private fun assertWindowsSize(surface: RenderSurface) {
        if(currWidth != surface.width || currHeight != surface.height) {
            currWidth  = surface.width
            currHeight = surface.height
            renWin.SetSize(currWidth, currHeight)
        }
    }

    override fun invoke(surface: RenderSurface, progression: Float) {
        assertWindowsSize(surface)

        val ang = 10 * Math.PI * 2 * progression
        cam.SetPosition(-5.0 * sin(ang), 0.0, -5.0 * cos(ang))
        cam.SetFocalPoint(0.0, 0.0, 0.0)

        renWin.Render()
    }
}