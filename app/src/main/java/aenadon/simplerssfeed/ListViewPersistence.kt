package aenadon.simplerssfeed

import android.widget.AdapterView

// These two need to be persisted on orientation change to ensure
// that the list doesn't need to reload all the news
data class ListViewPersistence(val newsAdapter: XMLNewsAdapter, val clickListener: AdapterView.OnItemClickListener)
