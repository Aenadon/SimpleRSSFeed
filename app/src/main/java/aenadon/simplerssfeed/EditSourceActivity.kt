package aenadon.simplerssfeed

import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ListView
import com.rometools.rome.io.ParsingFeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.net.MalformedURLException
import java.net.URL
import java.text.ParseException
import java.util.*

class EditSourceActivity : AppCompatActivity() {

    lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_source)

        val sourceListView = findViewById(R.id.source_editor_list) as ListView

        sharedPrefHelper = SharedPrefHelper(this@EditSourceActivity)
        // make an arraylist out of our list
        val feedSources: ArrayList<String> = ArrayList(sharedPrefHelper.getSourcesFromPrefs().map(URL::toString))

        val sourceAdapter = EditSourceAdapter(this@EditSourceActivity, feedSources)
        sourceListView.adapter = sourceAdapter

        // ask user if he really wanted deletion, then delete and push to SharedPrefs
        sourceListView.setOnItemLongClickListener { adapterView, view, position, rowId ->
            AlertDialog.Builder(this@EditSourceActivity)
                    .setTitle(getString(R.string.message_deleting_source_title))
                    .setMessage(String.format(getString(R.string.message_deleting_source_text), sourceAdapter.getItem(position)))
                    .setPositiveButton(android.R.string.ok, { dialogInterface, i ->
                        feedSources.removeAt(position)
                        sharedPrefHelper.updateSourceList(feedSources)
                        sourceAdapter.notifyDataSetChanged()
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            true
        }
    }



    @Suppress("unused") // this function is assigned as onClick handler in the layout XML
    fun addSource(view: View) {
        val urlBox = EditText(this@EditSourceActivity)
        urlBox.maxLines = 1
        urlBox.hint = "http://www.somewebsite.com/feeds/news.xml"
        urlBox.setHintTextColor(Color.LTGRAY)

        AlertDialog.Builder(this@EditSourceActivity)
                .setTitle(getString(R.string.enter_url))
                .setView(urlBox)
                .setPositiveButton(android.R.string.ok, { dialogInterface, i ->
                    ValidateURL(this@EditSourceActivity, sharedPrefHelper).execute(urlBox.text.toString())
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    class ValidateURL(val ctx: Context, val sharedPrefHelper: SharedPrefHelper) : AsyncTask<String, Void, Int>() {
        val VALID_URL = 0
        val INVALID_URL = 1
        val INVALID_RSS = 2

        override fun doInBackground(vararg params: String?): Int {
            val url: URL
            try {
                url = URL(params[0])
                SyndFeedInput().build(XmlReader(url)) // test parsing to see if successful

            } catch (e: MalformedURLException) {
                e.printStackTrace()
                return INVALID_URL
            } catch (e: ParseException) {
                e.printStackTrace()
                return INVALID_URL
            } catch (e: ParsingFeedException) {
                e.printStackTrace()
                return INVALID_RSS
            }
            // if no error has been thrown, the URL is a valid feed
            return VALID_URL
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            if (result != null && result != VALID_URL) {
                showErrorDialog(result)
            }
        }

        fun showErrorDialog(mode: Int) {
            val title: String
            val message: String
            if (mode == INVALID_URL) {
                title = ctx.getString(R.string.message_invalid_url_title)
                message = ctx.getString(R.string.message_invalid_url_text)
            } else if (mode == INVALID_RSS) {
                title = ctx.getString(R.string.message_invalid_rss_title)
                message = ctx.getString(R.string.message_invalid_rss_text)
            } else {
                return
            }

            AlertDialog.Builder(ctx)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
        }
    }


}
