import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication


fun main() = singleWindowApplication(
    state = WindowState(width = 1000.dp, height = 680.dp, position = WindowPosition(alignment = Alignment.Center)),
    resizable = true
) {
    App()
}