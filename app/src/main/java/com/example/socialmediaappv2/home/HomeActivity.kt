package com.example.socialmediaappv2.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.UserInfoPresenter
import com.example.socialmediaappv2.contract.Contract
import com.example.socialmediaappv2.explore.ExploreActivity
import com.example.socialmediaappv2.home.content.PublisherPictureContent
import com.example.socialmediaappv2.profile.ProfileActivity
import com.example.socialmediaappv2.upload.Camera2Activity
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home_scrolling.*


internal lateinit var presenter: Contract.UserInfoPresenter

class HomeActivity : AppCompatActivity(), Contract.MainView {

    private var recyclerViewAdapter: HomeRecyclerViewAdapter? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPresenter(UserInfoPresenter(this))
        Log.e("HomeActivity", "onCreate")
        if (!PublisherPictureContent.initLoaded) {
            PublisherPictureContent.initLoadImagesFromDatabase()
        }
        else PublisherPictureContent.loadRecentImages()

        setContentView(R.layout.activity_home)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title

        if (recyclerViewAdapter == null) {
            recyclerView = main_fragment.view as RecyclerView
            recyclerViewAdapter = (main_fragment.view as RecyclerView).adapter as HomeRecyclerViewAdapter
        }
        explore_button.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }
        upload_button.setOnClickListener {
            startActivity(Intent(this, Camera2Activity::class.java))
        }
        profile_button.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        Log.e("HomeActivity", "onResume")
        PublisherPictureContent.loadRecentImages()
        recyclerViewAdapter?.notifyDataSetChanged()
    }

    override fun setPresenter(_presenter: Contract.UserInfoPresenter) {
        presenter = _presenter
    }


}