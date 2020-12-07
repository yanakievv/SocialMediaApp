package com.example.socialmediaappv2.explore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.example.socialmediaappv2.PreviewImageFragment
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.data.ImageBitmap
import com.example.socialmediaappv2.data.SharedPreference
import com.example.socialmediaappv2.explore.content.PublicPictureContent
import com.example.socialmediaappv2.home.HomeActivity
import com.example.socialmediaappv2.profile.ProfileActivity
import com.example.socialmediaappv2.upload.Camera2Activity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_explore.*
import kotlinx.android.synthetic.main.content_explore_scrolling.*

class ExploreActivity : AppCompatActivity() {

    private var recyclerViewAdapter: ExploreRecyclerViewAdapter? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var sharedPref: SharedPreference
    private lateinit var skeletonScreen: RecyclerViewSkeletonScreen
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        sharedPref = SharedPreference(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContentView(R.layout.activity_explore)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title
        image_view.isClickable = false
        image_view.setTextColor(resources.getColor(R.color.colorBlack))
        image_view.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
        map_view.setTextColor(resources.getColor(R.color.colorWhite))
        map_view.setBackgroundColor(resources.getColor(R.color.colorLightBlack))

        if (recyclerViewAdapter == null) {
            recyclerView = main_fragment.view as RecyclerView
            recyclerViewAdapter = (main_fragment.view as RecyclerView).adapter as ExploreRecyclerViewAdapter
        }

        if (getLatLong() || (sharedPref.getString("lat") != null && sharedPref.getString("long") != null)) {
            PublicPictureContent.init(this)
        }
        else if (sharedPref.getString("lat") == null || sharedPref.getString("long") == null) {
            Toast.makeText(this, "Problem fetching coordinates. Go to Google Maps, locate your destination successfuly and try again.", Toast.LENGTH_LONG).show()
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            PublicPictureContent.initForce(this)
        }
        map_view.setOnClickListener {
            map_view.setTextColor(resources.getColor(R.color.colorBlack))
            map_view.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
            image_view.setTextColor(resources.getColor(R.color.colorWhite))
            image_view.setBackgroundColor(resources.getColor(R.color.colorLightBlack))
            startActivity(Intent(this, MapsActivity::class.java))
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
        explore_button.setOnClickListener {
            finish()
            startActivity(Intent(this, ExploreActivity::class.java))
        }
    }

    fun displayFragment(image: ImageBitmap) {
        val previewImageFragment = PreviewImageFragment(image)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(fragment_container.id, previewImageFragment).commit()

    }

    fun hideSkeleton() {
        image_view.isClickable = true
        map_view.isClickable = true
        image_view.background.alpha = 255
        map_view.background.alpha = 255
        skeletonScreen.hide()
    }

    fun showSkeleton() {
        image_view.isClickable = false
        map_view.isClickable = false
        image_view.background.alpha = 64
        map_view.background.alpha = 64
        map_view.visibility
        skeletonScreen = Skeleton.bind(recyclerView)
            .adapter(recyclerViewAdapter)
            .load(R.layout.fragment_explore_skeleton)
            .show();
    }
    private fun getLatLong(): Boolean {
        var coordsFetched = false
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return coordsFetched
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                sharedPref.save("lat", it.latitude.toString())
                sharedPref.save("long", it.longitude.toString())
                coordsFetched = true
            }
        }
        return coordsFetched
    }
}