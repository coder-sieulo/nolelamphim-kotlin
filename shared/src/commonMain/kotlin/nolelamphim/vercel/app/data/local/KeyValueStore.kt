package nolelamphim.vercel.app.data.local

/**
 * Minimal key-value abstraction so the shared module does not depend on a
 * specific storage library. Android uses SharedPreferences, iOS uses
 * NSUserDefaults and tests use [InMemoryKeyValueStore].
 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

expect fun createKeyValueStore(): KeyValueStore

class InMemoryKeyValueStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String): String? = values[key]

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }
}
