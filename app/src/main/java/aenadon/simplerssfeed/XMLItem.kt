package aenadon.simplerssfeed

import java.net.URL

// Holds the XML news elements to be displayed

data class XMLItem(
        val channelName: String,
        val newsTitle: String,
        val newsLink: URL,
        val newsDescription: String) {

}
