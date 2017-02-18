package aenadon.simplerssfeed

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {

    val FEED_SOURCES_FILENAME = "FEED_SOURCES"
    val FEED_SOURCES_KEY = "FEED_LIST"
    val FIRST_LAUNCH_KEY = "FIRST_LAUNCH"

    // this character is not (officially) allowed in a URL and is therefore a good separator
    val separator = "§"

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
            val defaultSourceList = arrayListOf(
                    "http://feeds.bbci.co.uk/news/world/rss.xml",
                    "http://feeds.bbci.co.uk/news/technology/rss.xml",
                    "http://rss.cnn.com/rss/edition.rss")

            // Put the default list together for storage
            feedSourceString = defaultSourceList.joinToString(separator)

            // store it
            sourceEditor.putString(FEED_SOURCES_KEY, feedSourceString)
            sourceEditor.putBoolean(FIRST_LAUNCH_KEY, false) // first launch is over
            sourceEditor.apply()

        } else {
            // retrieve the stored list from the prefs
            feedSourceString = feedSourcePrefs.getString(FEED_SOURCES_KEY, "")
        }

        if (!feedSourceString.isEmpty()) {
            // split string containing all sources, then add them to a list
            // which will be passed over to the Asynctask retrieving the feeds
            val rawSources = feedSourceString.split(separator)
            val sourceList = rawSources.map(::URL) // maps elements to ArrayList<URL>

            GetXML(this@MainActivity, sourceList.size).execute(sourceList)
        } else {
            Toast.makeText(this@MainActivity, getString(R.string.no_sources), Toast.LENGTH_LONG).show()
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
        // TODO make settings to set RSS sources

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    class GetXML(val ctx: Context, val listSize: Int) : AsyncTask<List<URL>, Int, List<XMLItem>>() {

        val LOG_TAG: String = GetXML::class.java.simpleName

        val waitingProgressDialog = ProgressDialog(ctx)

        override fun onPreExecute() {
            super.onPreExecute()
            waitingProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            waitingProgressDialog.max = listSize
            waitingProgressDialog.setTitle(ctx.getString(R.string.retrieving_news))
            waitingProgressDialog.show()
            waitingProgressDialog.setCancelable(false)
        }

        override fun doInBackground(vararg params: List<URL>?): List<XMLItem> {
            if (params.isEmpty()) {
                throw Error("GetXML Asynctask doInBackground params is empty!")
            }

            val urlList = params[0] ?: throw Error("GetXML Asynctask doInBackground params[0] is empty!")
            val entryList: ArrayList<XMLItem> = ArrayList()

            var progress = 1

            for (url in urlList) {
                val feed = SyndFeedInput().build(XmlReader(url)) // get XML to object

                // pick the needed info out of the object
                feed.entries.mapTo(entryList) {
                    XMLItem(
                            feed.title,
                            it.title,
                            URL(it.link),
                            it.description?.value ?: "" // can be null apparently!!!
                    )
                }
                publishProgress(progress++)
            }
            return entryList
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            val progress = values[0]
            if (progress != null) waitingProgressDialog.progress = progress
        }

        override fun onPostExecute(result: List<XMLItem>?) {
            super.onPostExecute(result)
            waitingProgressDialog.dismiss()

            if (result == null || result.isEmpty()) {
                return
            }
            val feedList = (ctx as Activity).findViewById(R.id.content_main) as ListView
            feedList.adapter = XMLNewsAdapter(result, ctx)
        }

    }
}
