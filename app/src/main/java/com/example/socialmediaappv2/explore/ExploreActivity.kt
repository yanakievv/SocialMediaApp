package com.example.socialmediaappv2.explore

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.explore.content.PublicPictureContent
import com.example.socialmediaappv2.home.HomeActivity
import com.example.socialmediaappv2.profile.ProfileActivity
import com.example.socialmediaappv2.upload.Camera2Activity
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_explore.*
import kotlinx.android.synthetic.main.content_explore_scrolling.*

class ExploreActivity : AppCompatActivity() {

    private var recyclerViewAdapter: ExploreRecyclerViewAdapter? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PublicPictureContent.init(50.0)

        setContentView(R.layout.activity_explore)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title

        if (recyclerViewAdapter == null) {
            recyclerView = main_fragment.view as RecyclerView
            recyclerViewAdapter = (main_fragment.view as RecyclerView).adapter as ExploreRecyclerViewAdapter
        }


        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            finish()
            startActivity(intent)
        }
        home_button.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        upload_button.setOnClickListener {
            startActivity(Intent(this, Camera2Activity::class.java))
        }
        profile_button.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}