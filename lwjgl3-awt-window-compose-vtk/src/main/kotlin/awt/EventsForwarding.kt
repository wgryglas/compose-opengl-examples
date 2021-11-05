package awt

import androidx.compose.ui.ComposeScene
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.mouse.MouseScrollOrientation
import androidx.compose.ui.input.mouse.MouseScrollUnit
import androidx.compose.ui.input.pointer.PointerEventType
import org.lwjgl.opengl.awt.AWTGLCanvas
import java.awt.event.*
import androidx.compose.ui.input.key.KeyEvent as ComposeKeyEvent


@OptIn(ExperimentalComposeUiApi::class)
fun AWTGLCanvas.forwardEventsToCompose(scene: ComposeScene) {
    fun MouseEvent.toOffset() = Offset(x.toFloat(), y.toFloat())

    addMouseMotionListener(object: MouseMotionListener {
        override fun mouseDragged(p0: MouseEvent) {
            scene.sendPointerEvent(
                position = p0.toOffset(),
                eventType = PointerEventType.Move,
                nativeEvent = p0
            )
        }
        override fun mouseMoved(p0: MouseEvent) {
            scene.sendPointerEvent(
                position = p0.toOffset(),
                eventType = PointerEventType.Move,
                nativeEvent = p0
            )
        }
    })

    addMouseListener(object : MouseListener {
        override fun mouseClicked(p0: MouseEvent) {
        }

        override fun mousePressed(p0: MouseEvent) {
            scene.sendPointerEvent(
                position = p0.toOffset(),
                eventType = PointerEventType.Press,
                nativeEvent = p0
            )
        }

        override fun mouseReleased(p0: MouseEvent) {
            scene.sendPointerEvent(
                position = p0.toOffset(),
                eventType = PointerEventType.Release,
                nativeEvent = p0
            )
        }

        override fun mouseEntered(p0: MouseEvent) {
            scene.sendPointerEvent(
                position = p0.toOffset(),
                eventType = PointerEventType.Enter,
                nativeEvent = p0
            )
        }

        override fun mouseExited(p0: MouseEvent) {
            scene.sendPointerEvent(
                position = p0.toOffset(),
                eventType = PointerEventType.Exit,
                nativeEvent = p0
            )
        }
    })

    addKeyListener(object: KeyListener {
        override fun keyPressed(e: KeyEvent) {
            scene.sendKeyEvent(ComposeKeyEvent(e))
        }

        override fun keyReleased(e: KeyEvent) {
            scene.sendKeyEvent(ComposeKeyEvent(e))
        }

        override fun keyTyped(e: KeyEvent) {
            scene.sendKeyEvent(ComposeKeyEvent(e))
        }
    })

    //TODO AWT does not recognize vert/hor scrolling
    addMouseWheelListener { event ->
        when(event.scrollType) {
            MouseWheelEvent.WHEEL_UNIT_SCROLL -> {
                scene.sendPointerScrollEvent(
                    position = event.toOffset(),
                    delta = MouseScrollUnit.Line(event.unitsToScroll.toFloat()),
                    orientation = MouseScrollOrientation.Vertical
                )
            }
//            MouseWheelEvent.WHEEL_BLOCK_SCROLL -> {
//                scene.sendPointerScrollEvent(
//                    position = event.toOffset(),
//                    delta = MouseScrollUnit.Line(event.unitsToScroll.toFloat()),
//                    orientation = MouseScrollOrientation.Vertical
//                )
//            }
        }
    }

}