package aenadon.simplerssfeed.activities

import aenadon.simplerssfeed.R
import aenadon.simplerssfeed.adapter.XMLNewsAdapter
import aenadon.simplerssfeed.models.XMLItem
import aenadon.simplerssfeed.utils.SharedPrefHelper
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        val INTENT_REFRESH_LIST = "INTENT_REFRESH_LIST"
    }

    lateinit var feedList: ListView

    // makes sure to refresh our list after sources have been edited
    var refreshReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        feedList = findViewById(R.id.content_main) as ListView

        if (lastCustomNonConfigurationInstance != null) {
            // if it was just an orientation change, set the old sourceListAdapter+clicklistener again
            val oldObjects = lastCustomNonConfigurationInstance as ListViewPersistence
            feedList.adapter = oldObjects.newsAdapter
            feedList.onItemClickListener = oldObjects.clickListener
        } else {
            // if it was more than an orientation change, go through
            // all the steps needed to get saved list of sources
            populateList()
        }
    }

    fun populateList() {
        val sharedPrefHelper = SharedPrefHelper(this@MainActivity)
        val feedSources = sharedPrefHelper.getSourcesFromPrefs()

        if (feedSources.isNotEmpty()) {
            GetXML(this@MainActivity, feedSources.size, feedList).execute(feedSources)
        } else {
            // if no sources specified, no need to do anything except informing the user
            Toast.makeText(this@MainActivity, getString(R.string.message_no_sources_title), Toast.LENGTH_LONG).show()

            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.message_no_news_title))
                    .setMessage(getString(R.string.message_no_news_text))
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
        }

        // receiver to refresh news list if sources were edited
        val intentFilter = IntentFilter()
        intentFilter.addAction(INTENT_REFRESH_LIST)

        // if source list has changed, please reload everything
        refreshReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                populateList()
            }
        }
        registerReceiver(refreshReceiver, intentFilter)
    }

    override fun onDestroy() {
        // kill the receiver on activity destruction
        if (refreshReceiver != null) {
            unregisterReceiver(refreshReceiver)
            refreshReceiver = null
        }
        super.onDestroy()
    }

    override fun onRetainCustomNonConfigurationInstance(): ListViewPersistence? {
        // return an object containing the sourceListAdapter and OnItemClickListener
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

        if (id == R.id.action_edit_sources) {
            startActivity(Intent(this@MainActivity, EditSourceActivity::class.java))
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

    // These two need to be persisted on orientation change to ensure
    // that the list doesn't need to reload all the news
    data class ListViewPersistence(val newsAdapter: XMLNewsAdapter, val clickListener: AdapterView.OnItemClickListener)
}
