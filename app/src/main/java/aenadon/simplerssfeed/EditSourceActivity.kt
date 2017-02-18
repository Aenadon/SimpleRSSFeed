package aenadon.simplerssfeed

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ListView
import android.widget.Toast
import java.net.URL
import java.util.*

class EditSourceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_source)

        val sourceListView = findViewById(R.id.source_editor_list) as ListView

        val sharedPrefHelper = SharedPrefHelper(this@EditSourceActivity)
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
        Toast.makeText(this, "It works", Toast.LENGTH_LONG).show()
    }
}
