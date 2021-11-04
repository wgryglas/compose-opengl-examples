import org.lwjgl.opengl.GL30
import kotlin.math.abs
import kotlin.math.sin

class RawOGLCommands : RenderCommands {

    override fun prerenderInit(surface: RenderSurface) {
        GL30.glClearColor(0.2f, 0.2f, 1f, 1f)
    }

    override fun invoke(surface: RenderSurface, progression: Float) {
            val aspect = surface.width.toFloat() / surface.height
            val width2 = abs(sin(progression * 30))
            GL30.glClear(GL30.GL_COLOR_BUFFER_BIT)
            GL30.glViewport(0, 0, surface.width, surface.height)
            GL30.glBegin(GL30.GL_QUADS)
                GL30.glColor3f(0.4f, 0.6f, 0.8f)
                GL30.glVertex2f(-0.75f * width2 / aspect, 0.0f)
                GL30.glVertex2f(0f, -0.75f)
                GL30.glVertex2f(+0.75f * width2 / aspect, 0f)
                GL30.glVertex2f(0f, +0.75f)
            GL30.glEnd()
    }
}

