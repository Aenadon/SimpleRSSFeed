package aenadon.simplerssfeed.adapter

import aenadon.simplerssfeed.R
import aenadon.simplerssfeed.models.XMLItem
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.DateFormat


class XMLNewsAdapter(val newsList: List<XMLItem>, val ctx: Context) : BaseAdapter() {

    override fun getItem(position: Int): XMLItem {
        return newsList[position] // return the corresponding XMLItem from the list
    }

    override fun getItemId(position: Int): Long {
        return 0 // the items don't have an ID
    }

    override fun getCount(): Int {
        return newsList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        var displayView = convertView

        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (displayView == null) {
            @SuppressLint("InflateParams") // specifying "parent" causes crash
            displayView = inflater.inflate(R.layout.news_list_item, null)
            viewHolder = ViewHolder( // initialize the views in ViewHolder
                    displayView.findViewById(R.id.list_item_title) as TextView,
                    displayView.findViewById(R.id.list_item_description) as TextView,
                    displayView.findViewById(R.id.list_item_channel_name) as TextView,
                    displayView.findViewById(R.id.list_item_date) as TextView
            )

            displayView.tag = viewHolder
        } else {
            viewHolder = displayView.tag as ViewHolder
        }

        // Parse description to stay below 140 characters

        val newsDescription = newsList[position].newsDescription
        val slicedDescription: String
        if (newsDescription.length <= 140) {
            slicedDescription = newsDescription // if string is already 140, don't slice it
        } else {
            slicedDescription = newsDescription.substring(0, 140) + "…" // &#8230;
            // if string is larger than 140, truncate it and add … (does not happen often)
        }

        // Assign the news item contents to the respective TextViews
        viewHolder.title.text = newsList[position].newsTitle
        viewHolder.description.text = slicedDescription
        viewHolder.channelName.text = newsList[position].channelName
        viewHolder.dateText.text = DateFormat.getDateTimeInstance().format(newsList[position].date)

        return displayView!! // displayView will be definitely initialized by now, so we can use !!
    }


    // This is the class that will hold the view IDs
    data class ViewHolder(var title: TextView, var description: TextView, var channelName: TextView, var dateText: TextView)

}
