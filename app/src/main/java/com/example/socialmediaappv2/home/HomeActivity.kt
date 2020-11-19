package com.example.socialmediaappv2.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.example.socialmediaappv2.PreviewImageFragment
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.data.ImageBitmap
import com.example.socialmediaappv2.data.ImageModel
import com.example.socialmediaappv2.data.SharedPreference
import com.example.socialmediaappv2.explore.ExploreActivity
import com.example.socialmediaappv2.explore.MapsActivity
import com.example.socialmediaappv2.home.content.PublisherPictureContent
import com.example.socialmediaappv2.profile.ProfileActivity
import com.example.socialmediaappv2.upload.Camera2Activity
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.activity_home.explore_button
import kotlinx.android.synthetic.main.activity_home.fab
import kotlinx.android.synthetic.main.activity_home.fragment_container
import kotlinx.android.synthetic.main.activity_home.home_button
import kotlinx.android.synthetic.main.activity_home.profile_button
import kotlinx.android.synthetic.main.activity_home.upload_button
import kotlinx.android.synthetic.main.content_home_scrolling.*

internal lateinit var sharedPref: SharedPreference

class HomeActivity : AppCompatActivity() {

    private var recyclerViewAdapter: HomeRecyclerViewAdapter? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var skeletonScreen: RecyclerViewSkeletonScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = SharedPreference(this)
        Log.e("HomeActivity", "onCreate")
        if (!intent.hasExtra("userId")) {
            if (!PublisherPictureContent.initLoaded) {
                PublisherPictureContent.initLoadImagesFromDatabase(sharedPref.getString("publisherId")!!, this)
            } else PublisherPictureContent.loadRecentImages(sharedPref.getString("publisherId")!!, this)
            title = "My Posts"
        }
        else {
            PublisherPictureContent.initLoadImagesFromDatabase(intent.getStringExtra("userId")!!, this)
            title = intent.getStringExtra("displayName") + "'s Posts"
        }

        setContentView(R.layout.activity_home)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title

        if (PublisherPictureContent.isCurrentUser) {
            fab.visibility = View.INVISIBLE
        }
        else fab.visibility = View.VISIBLE

        if (recyclerViewAdapter == null) {
            recyclerView = main_fragment.view as RecyclerView
            recyclerViewAdapter = (main_fragment.view as RecyclerView).adapter as HomeRecyclerViewAdapter
        }

        fab.setOnClickListener {
            PublisherPictureContent.initLoadImagesFromDatabase(sharedPref.getString("publisherId")!!, this)
            fab.visibility = View.INVISIBLE
        }
        explore_button.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }
        upload_button.setOnClickListener {
            if (!PublisherPictureContent.isCurrentUser) {
                finish()
            }
            startActivity(Intent(this, Camera2Activity::class.java))
        }
        profile_button.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        home_button.setOnClickListener {
            finish()
            startActivity(Intent(this, HomeActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        Log.e("HomeActivity", "onResume")
        if (PublisherPictureContent.isCurrentUser && sharedPref.getInt("posts") != 0) {
            PublisherPictureContent.loadRecentImages(sharedPref.getString("publisherId")!!, this)
        }

    }

    fun displayFragment(image: ImageBitmap) {
        val previewImageFragment = PreviewImageFragment(image)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(fragment_container.id, previewImageFragment).commit()
    }

    fun notifyAdapter() {
        recyclerViewAdapter?.notifyDataSetChanged()
    }

    fun hideSkeleton() {
        skeletonScreen.hide()
    }

    fun showSkeleton() {
        skeletonScreen = Skeleton.bind(recyclerView)
            .adapter(recyclerViewAdapter)
            .load(R.layout.fragment_explore_skeleton)
            .show()
    }

}