package aenadon.simplerssfeed

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast

class EditSourceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_source)
    }

    @Suppress("unused") // this function is assigned as onClick handler in the layout XML
    fun addSource(view: View) {
        Toast.makeText(this, "It works", Toast.LENGTH_LONG).show()
    }
}
