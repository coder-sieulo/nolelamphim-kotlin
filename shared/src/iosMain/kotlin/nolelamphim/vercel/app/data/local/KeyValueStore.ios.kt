package nolelamphim.vercel.app.data.local

import platform.Foundation.NSUserDefaults

actual fun createKeyValueStore(): KeyValueStore = NSUserDefaultsKeyValueStore()

private class NSUserDefaultsKeyValueStore : KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}
