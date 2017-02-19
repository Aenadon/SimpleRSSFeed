package aenadon.simplerssfeed.models

import java.net.URL
import java.util.*

// Holds the XML news elements to be displayed

data class XMLItem(
        val channelName: String,
        val newsTitle: String,
        val newsLink: URL,
        val newsDescription: String,
        val date: Date) : Comparable<XMLItem> {

    override fun compareTo(other: XMLItem): Int {
        return this.date.compareTo(other.date)
    }

}
