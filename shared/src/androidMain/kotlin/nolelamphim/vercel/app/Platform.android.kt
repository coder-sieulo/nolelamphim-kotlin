package nolelamphim.vercel.app

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import nolelamphim.vercel.app.data.local.AndroidAppContext

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isTv: Boolean
        get() {
            val ctx = AndroidAppContext.current()
            if (ctx != null) {
                val isTvPackage = ctx.packageName.endsWith(".tv")
                val pm = ctx.packageManager
                val hasLeanback = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                val hasTvFeature = pm.hasSystemFeature("android.hardware.type.television")
                val uiModeManager = ctx.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
                val isUiModeTv = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
                val isConfigTv = (ctx.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) ==
                    Configuration.UI_MODE_TYPE_TELEVISION

                if (isTvPackage || hasLeanback || hasTvFeature || isUiModeTv || isConfigTv) {
                    return true
                }
            }
            return try {
                (android.content.res.Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) ==
                    Configuration.UI_MODE_TYPE_TELEVISION
            } catch (_: Exception) {
                false
            }
        }
}

actual fun getPlatform(): Platform = AndroidPlatform()