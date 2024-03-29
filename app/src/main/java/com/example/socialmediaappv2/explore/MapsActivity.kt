package com.example.socialmediaappv2.explore

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.socialmediaappv2.PreviewImageFragment
import com.example.socialmediaappv2.R
import com.example.socialmediaappv2.data.ImageBitmap
import com.example.socialmediaappv2.data.ImageModel
import com.example.socialmediaappv2.data.SharedPreference
import com.example.socialmediaappv2.explore.content.PublicPictureContent
import com.example.socialmediaappv2.home.HomeActivity
import com.example.socialmediaappv2.profile.ProfileActivity
import com.example.socialmediaappv2.upload.Camera2Activity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.slider.Slider
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_maps.explore_button
import kotlinx.android.synthetic.main.activity_maps.home_button
import kotlinx.android.synthetic.main.activity_maps.image_view
import kotlinx.android.synthetic.main.activity_maps.map_view
import kotlinx.android.synthetic.main.activity_maps.profile_button
import kotlinx.android.synthetic.main.activity_maps.upload_button
import kotlin.math.min


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var sharedPref: SharedPreference
    private lateinit var current: LatLng
    private var markers: MutableList<Marker> = ArrayList()
    private var index = 0
    private var tempIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        sharedPref = SharedPreference(this)
        PublicPictureContent.init(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        map_view.isClickable = false
        image_view.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }
        explore_button.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }
        upload_button.setOnClickListener {
            startActivity(Intent(this, Camera2Activity::class.java))
        }
        home_button.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        profile_button.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        continuousSlider.addOnChangeListener(object: Slider.OnChangeListener{
            override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
                Log.d("SLIDER", value.toString())
                if (index < markers.size - 1 && value >= (markers[index].tag as ImageBitmap).getDistance()) {
                    while (index < markers.size - 1 && (markers[index].tag as ImageBitmap).getDistance() < value) {
                        index++
                    }
                    for (i in tempIndex until index) {
                        markers[i].isVisible = true
                        Log.e("MARKER_SHOW", (markers[i].tag as ImageBitmap).getDistance().toString())
                    }
                    tempIndex = index
                }
                else if (index > 0 && value < (markers[index - 1].tag as ImageBitmap).getDistance()) {
                    while (index > 0 && (markers[index - 1].tag as ImageBitmap).getDistance() > value) {
                        index--
                    }
                    for (i in index until tempIndex) {
                        markers[i].isVisible = false
                        Log.e("MARKER_HIDE", (markers[i].tag as ImageBitmap).getDistance().toString())
                    }
                    tempIndex = index
                }
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        current = LatLng(
            sharedPref.getString("lat")!!.toDouble(),
            sharedPref.getString("long")!!.toDouble()
        )
        val currentLocation = mMap.addMarker(
            MarkerOptions().position(current).title("Current Location")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current))
        for (i in PublicPictureContent.SORTED_IMAGES) {
            i.updateBitmap()
            val marker = mMap.addMarker(
                MarkerOptions().position(LatLng(i.imageModel.latitude, i.imageModel.longitude))
                    .title(i.imageModel.publisherDisplayName).icon(
                        BitmapDescriptorFactory.fromBitmap(
                            i.getThumbnail()?.let {
                                CircleBubbleTransformation().transform(
                                    it
                                )
                            }
                        )
                    ).draggable(false)
            )
            marker.tag = i
            if (i.getDistance() <= continuousSlider.value) {
                index++
            }
            else marker.isVisible = false
            markers.add(marker)
            Log.e("IMG_ID", i.imageModel.picId.toString())
            Log.e("IMG_DIST", i.getDistance().toString())
            Log.e("INDEX", index.toString())

        }
        tempIndex = index

        mMap.setOnInfoWindowClickListener {
            if (it != currentLocation) {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("userId", (it.tag as ImageBitmap).imageModel.publisherId)
                startActivity(intent)
            }
        }
    }

    fun displayFragment(image: ImageBitmap) {
        val previewImageFragment = PreviewImageFragment(image)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(fragment_container.id, previewImageFragment).commit()

    }
}

class CircleBubbleTransformation : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)
        val r = size / 2f
        val output = Bitmap.createBitmap(
            size + triangleMargin,
            size + triangleMargin,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val paintBorder = Paint()
        paintBorder.isAntiAlias = true
        paintBorder.color = Color.parseColor("#D9513D")
        paintBorder.strokeWidth = margin.toFloat()
        canvas.drawCircle(r, r, r - margin, paintBorder)
        val trianglePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        trianglePaint.strokeWidth = 2F
        trianglePaint.color = Color.parseColor("#D9513D")
        trianglePaint.style = Paint.Style.FILL_AND_STROKE
        trianglePaint.isAntiAlias = true
        val triangle = Path()
        triangle.fillType = Path.FillType.EVEN_ODD
        triangle.moveTo(
            (size - margin).toFloat(),
            (size / 2).toFloat()
        )
        triangle.lineTo(
            (size / 2).toFloat(),
            (size + triangleMargin).toFloat()
        )
        triangle.lineTo(margin.toFloat(), (size / 2).toFloat())
        triangle.close()
        canvas.drawPath(triangle, trianglePaint)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        canvas.drawCircle(r, r, r - photoMargin, paint)
        if (source != output) {
            source.recycle()
        }
        return output
    }

    override fun key(): String {
        return "circlebubble"
    }

    companion object {
        private const val photoMargin = 30
        private const val margin = 20
        private const val triangleMargin = 10
    }
}