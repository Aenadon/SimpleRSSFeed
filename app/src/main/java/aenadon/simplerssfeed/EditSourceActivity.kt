package aenadon.simplerssfeed

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ListView
import android.widget.Toast
import java.net.URL

class EditSourceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_source)

        val sourceListView = findViewById(R.id.source_editor_list) as ListView

        val sharedPrefHelper = SharedPrefHelper(this@EditSourceActivity)
        val feedSources: List<String> = sharedPrefHelper.getSourcesFromPrefs().map(URL::toString)

        val sourceAdapter = EditSourceAdapter(this@EditSourceActivity, feedSources)
        sourceListView.adapter = sourceAdapter
    }

    @Suppress("unused") // this function is assigned as onClick handler in the layout XML
    fun addSource(view: View) {
        Toast.makeText(this, "It works", Toast.LENGTH_LONG).show()
    }
}
