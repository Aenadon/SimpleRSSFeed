package aenadon.simplerssfeed

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
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

    lateinit var feedList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        feedList = findViewById(aenadon.simplerssfeed.R.id.content_main) as ListView


        if (lastCustomNonConfigurationInstance != null) {
            // if it was just an orientation change, set the old adapter+clicklistener again
            val oldObjects = lastCustomNonConfigurationInstance as ListViewPersistence
            feedList.adapter = oldObjects.newsAdapter
            feedList.onItemClickListener = oldObjects.clickListener
        } else {
            // if it was more than an orientation change, go through all the steps needed
            // get saved list of sources
            val feedSourcePrefs = this.getSharedPreferences(FEED_SOURCES_FILENAME, Context.MODE_PRIVATE)
            val firstLaunch = feedSourcePrefs.getBoolean(FIRST_LAUNCH_KEY, true)

            val feedSourceString: String

            if (firstLaunch) {
                // if it's the first launch, preset some default sources
                feedSourceString = presetSources(feedSourcePrefs)
            } else {
                // retrieve the stored list from the prefs
                feedSourceString = feedSourcePrefs.getString(FEED_SOURCES_KEY, "")
            }

            if (!feedSourceString.isEmpty()) {
                // split string containing all sources, then add them to a list
                // which will be passed over to the Asynctask retrieving the feeds
                val rawSources = feedSourceString.split(separator)
                val sourceList = rawSources.map(::URL) // maps elements to ArrayList<URL>

                GetXML(this@MainActivity, sourceList.size, feedList).execute(sourceList)
            } else {
                // if no sources specified, no need to do anything except informing the user
                Toast.makeText(this@MainActivity, getString(R.string.message_no_sources_title), Toast.LENGTH_LONG).show()

                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.message_no_news_title))
                        .setMessage(getString(R.string.message_no_news_text))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
            }
        }
    }

    fun presetSources(feedSourcePrefs: SharedPreferences): String {
        val sourceEditor = feedSourcePrefs.edit()
        val defaultSourceList = arrayListOf(
                "http://feeds.bbci.co.uk/news/world/rss.xml",
                "http://feeds.bbci.co.uk/news/technology/rss.xml"/*,
                "http://rss.cnn.com/rss/edition.rss"*/) // TODO disabled for debugging, it takes too long to load

        // Put the default list together for storage
        val feedSourceString = defaultSourceList.joinToString(separator)

        // store it
        sourceEditor.putString(FEED_SOURCES_KEY, feedSourceString)
        sourceEditor.putBoolean(FIRST_LAUNCH_KEY, false) // first launch is over
        sourceEditor.apply()

        return feedSourceString
    }

    override fun onRetainCustomNonConfigurationInstance(): ListViewPersistence? {
        // return an object containing the adapter and OnItemClickListener
        // so we can apply these back to the feedList after orientation change
        return ListViewPersistence(feedList.adapter as XMLNewsAdapter, feedList.onItemClickListener)
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

    class GetXML(val ctx: Context, val listSize: Int, val feedList: ListView) : AsyncTask<List<URL>, Int, List<XMLItem>>() {

        // This is the dialog showing the progress during the loading operation
        val waitingProgressDialog = ProgressDialog(ctx)

        override fun onPreExecute() {
            super.onPreExecute()
            waitingProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            waitingProgressDialog.max = listSize
            waitingProgressDialog.setTitle(ctx.getString(R.string.retrieving_news))
            waitingProgressDialog.setCancelable(false)
            waitingProgressDialog.show()
        }

        override fun doInBackground(vararg params: List<URL>?): List<XMLItem> {
            if (params.isEmpty()) {
                // GetXML is only initialized in one place with the parameter given
                throw Error("GetXML Asynctask doInBackground params is empty!")
            }

            // Should never happen, this is being prevented before GetXML initialization
            val urlList = params[0] ?: throw Error("GetXML Asynctask doInBackground params[0] is empty!")
            val entryList: ArrayList<XMLItem> = ArrayList() // list holding the news items

            var progress = 1

            for (url in urlList) {
                val feed = SyndFeedInput().build(XmlReader(url)) // get XML to object

                // pick the needed info out of the object
                feed.entries.mapTo(entryList) {
                    XMLItem(
                            feed.title,
                            it.title,
                            URL(it.link),
                            it.description?.value ?: "", // can be null apparently!!!
                            it.publishedDate ?: Date() // if date is not specified, say "now"
                    )
                }
                Collections.sort(entryList, Collections.reverseOrder()) // sort list after date but reversed (newest at top)
                publishProgress(progress++) // publish current progress count, then increment
            }
            return entryList
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            val progress = values[0]
            if (progress != null) waitingProgressDialog.progress = progress // put current progress on screen
        }

        override fun onPostExecute(result: List<XMLItem>?) {
            super.onPostExecute(result)
            waitingProgressDialog.dismiss()

            if (result == null) {
                // Can not happen
                throw Error("doInBackground result empty")
            } else if (result.isEmpty()) {
                // this can only happen if all specified sources have no news at all
                AlertDialog.Builder(ctx)
                        .setTitle(ctx.getString(R.string.message_no_news_title))
                        .setMessage(ctx.getString(R.string.message_no_news_text))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                return
            }
            val newsAdapter = XMLNewsAdapter(result, ctx)
            feedList.adapter = newsAdapter
            feedList.setOnItemClickListener { adapterView, view, position, rowId ->
                // on click, open the corresponding URL
                val url: URL = (feedList.getItemAtPosition(position) as XMLItem).newsLink

                val openBrowser: Intent = Intent(Intent.ACTION_VIEW)
                // as we have a correctly formed URL, the conversion to URI should be no problem
                openBrowser.data = Uri.parse(url.toString())
                ctx.startActivity(openBrowser)
            }

        }

    }
}
