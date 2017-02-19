package aenadon.simplerssfeed.utils

import android.content.Context
import android.content.SharedPreferences
import java.net.URL
import java.util.*

class SharedPrefHelper(ctx: Context) {
    val FEED_SOURCES_FILENAME = "FEED_SOURCES"
    val FEED_SOURCES_KEY = "FEED_LIST"
    val FIRST_LAUNCH_KEY = "FIRST_LAUNCH"

    // this character is not (officially) allowed in a URL and is therefore a good separator
    val separator = "§"

    val prefs: SharedPreferences
    init {
        prefs = ctx.getSharedPreferences(FEED_SOURCES_FILENAME, Context.MODE_PRIVATE)
    }

    fun isFirstLaunch() : Boolean {
        return prefs.getBoolean(FIRST_LAUNCH_KEY, true)
    }

    fun getSourcesFromPrefs() : List<URL> {
        if (prefs.getBoolean(FIRST_LAUNCH_KEY, true)) {
            return presetAndGetSources()
        } else {
            val prefString = prefs.getString(FEED_SOURCES_KEY, "")
            return prefString.split(separator).map(::URL)
        }
    }

    // this function will silently set preset sources if the app has never been launched before
    private fun presetAndGetSources() : List<URL> {
        val sourceEditor = prefs.edit()
        val defaultSourceList = arrayListOf(
                "http://feeds.bbci.co.uk/news/world/rss.xml",
                "http://feeds.bbci.co.uk/news/technology/rss.xml",
                "http://rss.cnn.com/rss/edition.rss")

        // Put the default list together for storage
        val feedSourceString = defaultSourceList.joinToString(separator)

        // store it
        sourceEditor.putString(FEED_SOURCES_KEY, feedSourceString)
        sourceEditor.putBoolean(FIRST_LAUNCH_KEY, false) // first launch is over
        sourceEditor.apply()

        return defaultSourceList.map(::URL) // return URL list
    }

    fun updateSourceList(feedSources: ArrayList<String>) {
        val editor = prefs.edit()
        editor.putString(FEED_SOURCES_KEY, feedSources.joinToString(separator))
        editor.apply()
    }

    fun addURLToSourceList(url: URL) { // asking for URL as parameter to prevent unintended unchecked calls
        var prevSources = prefs.getString(FEED_SOURCES_KEY, "")
        if (prevSources.isNotEmpty()) {
            prevSources += separator // if the string is not empty, add the separator now
        }

        val editor = prefs.edit()
        editor.putString(FEED_SOURCES_KEY, prevSources + url.toString())
        editor.apply()
    }
}
