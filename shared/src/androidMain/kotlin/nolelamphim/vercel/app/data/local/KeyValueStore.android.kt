package nolelamphim.vercel.app.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Holds the application context so the shared module can build a
 * SharedPreferences-backed store without leaking an Activity.
 *
 * Initialised from `MainActivity.onCreate` before the Compose tree is created.
 */
object AndroidAppContext {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    internal fun current(): Context? = appContext
}

actual fun createKeyValueStore(): KeyValueStore {
    // Previews (and unit tests) may not initialise the context; fall back to
    // memory so the UI can still render.
    val context = AndroidAppContext.current() ?: return InMemoryKeyValueStore()
    return SharedPreferencesKeyValueStore(context)
}

private class SharedPreferencesKeyValueStore(context: Context) : KeyValueStore {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nolelamphim", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
