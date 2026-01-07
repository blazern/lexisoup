package blazern.lexisoup.feature.home.ui


import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

@Suppress("MagicNumber")
internal val IconSwitch: ImageVector
    get() {
        if (_switchAlt != null) {
            return _switchAlt!!
        }
        _switchAlt = materialIcon(name = "Filled.Switch") {
            materialPath {
                moveTo(18.0f, 12.0f)
                lineToRelative(4.0f, -4.0f)
                lineToRelative(-4.0f, -4.0f)
                lineToRelative(0.0f, 3.0f)
                lineToRelative(-15.0f, 0.0f)
                lineToRelative(0.0f, 2.0f)
                lineToRelative(15.0f, 0.0f)
                close()
            }
            materialPath {
                moveTo(6.0f, 12.0f)
                lineToRelative(-4.0f, 4.0f)
                lineToRelative(4.0f, 4.0f)
                lineToRelative(0.0f, -3.0f)
                lineToRelative(15.0f, 0.0f)
                lineToRelative(0.0f, -2.0f)
                lineToRelative(-15.0f, 0.0f)
                close()
            }
        }
        return _switchAlt!!
    }

private var _switchAlt: ImageVector? = null
