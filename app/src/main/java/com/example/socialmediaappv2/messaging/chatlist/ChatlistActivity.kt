package com.example.socialmediaappv2.messaging.chatlist

import android.os.Bundle
import com.google.android.material.appbar.CollapsingToolbarLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmediaappv2.R
import kotlinx.android.synthetic.main.content_chatlist_scrolling.*

class ChatlistActivity : AppCompatActivity() {

    private var recyclerViewAdapter: ChatlistRecyclerViewAdapter? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatlist)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (recyclerViewAdapter == null) {
            recyclerView = main_fragment.view as RecyclerView
            recyclerViewAdapter =
                (main_fragment.view as RecyclerView).adapter as ChatlistRecyclerViewAdapter
        }
    }
}