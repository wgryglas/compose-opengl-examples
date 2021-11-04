package awt

import App
import RenderSurface
import androidx.compose.ui.ComposeScene
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skia.*
import org.jetbrains.skiko.FrameDispatcher
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.*
import javax.swing.JFrame
import javax.swing.SwingUtilities

private fun createSurface(width: Int, height: Int, context: DirectContext): Surface {
    val fbId = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING)
    val renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, fbId, FramebufferFormat.GR_GL_RGBA8)
    return Surface.makeFromBackendRenderTarget(
        context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.sRGB
    )
}

fun main(args: Array<String>) {
    val frame = JFrame("Compose-AWT-${args[1].uppercase()} Demo")
    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    frame.layout = BorderLayout()
    frame.preferredSize = Dimension(600, 400)

    val data = GLData()
//    data.majorVersion = 3
//    data.minorVersion = 3
//    data.profile = GLData.Profile.CORE
//    data.samples = 4


    val canvas = object : AWTGLCanvas(data) {
        init {
            preferredSize = Dimension(400, 400)
        }

        var skContext: DirectContext? = null
        var surface: Surface? = null
        var composeScene: ComposeScene? = null

        val renSurf = RenderSurface(0, 0)
        val glfwDispatcher = Dispatchers.Swing

        val renderCommands = RenderCommands.getByClassName(args[0]) ?: throw RuntimeException("Unknown name of example: ${args[0]}")

        var progress = 0

        override fun initGL() {
            GL.createCapabilities()

            skContext = DirectContext.makeGL()
            surface = createSurface(width, height, skContext!!) // Skia Surface, bound to the OpenGL framebuffer

            val frameDispatcher = FrameDispatcher(glfwDispatcher) {
                render()
                repaint()
            }

            composeScene = ComposeScene(
                glfwDispatcher,
                density= Density(1f,1f),
                invalidate = frameDispatcher::scheduleFrame
            ).also { scene->
                scene.setContent { App() }
                forwardEventsToCompose(scene)
            }

            addComponentListener(object : ComponentAdapter(){
                override fun componentResized(e: ComponentEvent) {
                    surface?.close()
                    surface = skContext?.let { createSurface(width, height, it) }

                    renSurf.setSize(width, height)

                    render()
                }
            })

            renSurf.setSize(width, height)
            renderCommands.prerenderInit(renSurf)
        }

        override fun paintGL() {
            GL30.glActiveTexture(GL30.GL_TEXTURE0)
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0)
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0)
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
            GL30.glUseProgram(0)
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0)
            GL30.glClear(GL30.GL_COLOR_BUFFER_BIT)

            renderCommands.invoke(renSurf, (progress++).toFloat() / 1_000)

            composeScene?.apply {
                constraints = Constraints(maxWidth = width, maxHeight = height)
                surface?.let { this@apply.render(it.canvas, System.nanoTime()) }
                skContext?.flush()
            }

            swapBuffers()
        }
    }

    frame.contentPane.add(canvas, BorderLayout.CENTER)
    frame.pack()
    frame.isVisible = true
    frame.transferFocus()

    frame.addWindowListener(object: WindowAdapter() {
        override fun windowClosing(p0: WindowEvent?) {
            canvas.composeScene?.close()
        }
    })

    SwingUtilities.invokeLater {
        if (canvas.isValid) {
            canvas.render()
        }
    }
}