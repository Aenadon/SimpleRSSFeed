package aenadon.simplerssfeed

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.*

class EditSourceAdapter(val ctx: Context, val sourceList: ArrayList<String>) : BaseAdapter() {

    override fun getItem(position: Int): String {
        return sourceList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0 // no IDs used
    }

    override fun getCount(): Int {
        return sourceList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        var displayView = convertView

        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (displayView == null) {
            @SuppressLint("InflateParams") // specifying "parent" causes crash
            displayView = inflater.inflate(android.R.layout.simple_list_item_1, null)
            viewHolder = ViewHolder(displayView.findViewById(android.R.id.text1) as TextView)
            displayView.tag = viewHolder
        } else {
            viewHolder = displayView.tag as ViewHolder
        }

        viewHolder.urlView.text = sourceList[position]

        return displayView!!
    }

    // ViewHolder holding ID of TextView
    data class ViewHolder(val urlView: TextView)

}
