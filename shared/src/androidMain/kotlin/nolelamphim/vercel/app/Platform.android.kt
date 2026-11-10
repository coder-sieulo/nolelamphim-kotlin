package nolelamphim.vercel.app

import android.os.Build
import android.content.res.Configuration
import android.content.res.Resources

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isTv: Boolean =
        Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_TYPE_MASK ==
            Configuration.UI_MODE_TYPE_TELEVISION
}

actual fun getPlatform(): Platform = AndroidPlatform()