package aenadon.simplerssfeed

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import java.net.URL

class MainActivity : AppCompatActivity() {

    val FEED_SOURCES_FILENAME = "FEED_SOURCES"
    val FEED_SOURCES_KEY = "FEED_LIST"
    val FIRST_LAUNCH_KEY = "FIRST_LAUNCH"

    // these two characters are not (officially) allowed in a URL and are therefore good separators
    val titleSeparator = "^"
    val entrySeparator = "§"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // get saved list of sources
        val feedSourcePrefs = this.getSharedPreferences(FEED_SOURCES_FILENAME, Context.MODE_PRIVATE)
        val firstLaunch = feedSourcePrefs.getBoolean(FIRST_LAUNCH_KEY, true)

        val feedSourceString: String

        if (firstLaunch) {
            // if it's the first launch, preset some default sources
            val sourceEditor = feedSourcePrefs.edit()
            // The titles are hardcoded here, but when a user adds a feed source,
            // the title will be retrieved from the feed's XML (channel->title tag)
            val defaultSourceList = arrayListOf(
                    "BBC News - World" + titleSeparator + "http://feeds.bbci.co.uk/news/world/rss.xml",
                    "BBC News - Technology" + titleSeparator + "http://feeds.bbci.co.uk/news/technology/rss.xml",
                    "CNN.com - RSS Channel - Intl Homepage - News" + titleSeparator + "http://rss.cnn.com/rss/edition.rss")

            // Put the default list together for storage
            feedSourceString = defaultSourceList.joinToString { entrySeparator }

            // store it
            sourceEditor.putString(FEED_SOURCES_KEY, feedSourceString)
            sourceEditor.apply()


        } else {
            // retrieve the stored list from the prefs
            feedSourceString = feedSourcePrefs.getString(FEED_SOURCES_KEY, "")
        }

        if (!feedSourceString.isEmpty()) {
            // split string containing all sources, then add them to the list
            // which will be passed over to the Asynctask retrieving the feeds
            val rawSources = feedSourceString.split(entrySeparator)
            val sourceList = rawSources.map(::URL) // maps elements to ArrayList<URL>
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
