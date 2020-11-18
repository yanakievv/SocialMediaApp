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
        //currentLocation.tag = ImageBitmap(ImageModel(0, "0", "0", "0", "0", 0.0, 0.0, 0))
        currentLocation.isDraggable = false
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current))

        for (i in PublicPictureContent.SORTED_IMAGES) {
            val pos = LatLng(i.imageModel.latitude, i.imageModel.longitude)
            mMap.addMarker(
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
            ).tag = i
            Log.e("IMG_ID", i.imageModel.picId.toString())
        }

        /*mMap.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                displayFragment(marker.tag as ImageBitmap)
            }

            override fun onMarkerDragEnd(marker: Marker) {
                mMap.addMarker(
                    MarkerOptions().position(LatLng((marker.tag as ImageBitmap).imageModel.latitude, (marker.tag as ImageBitmap).imageModel.longitude))
                        .title((marker.tag as ImageBitmap).imageModel.publisherDisplayName).icon(
                            BitmapDescriptorFactory.fromBitmap(
                                    CircleBubbleTransformation().transform(
                                        (marker.tag as ImageBitmap).getThumbnail()!!
                                    )
                            )
                        ).draggable(true)
                ).tag = (marker.tag as ImageBitmap)
                marker.remove()
            }

            override fun onMarkerDrag(marker: Marker) {
                // TODO Auto-generated method stub
            }
        })   USED TO WORK ----  W/Bitmap: Called getWidth() on a recycle()'d bitmap! This is undefined behavior!
                                Called getHeight() on a recycle()'d bitmap! This is undefined behavior!
                                W/Bitmap: Called getConfig() on a recycle()'d bitmap! This is undefined behavior!
                                A/Bitmap: Error, cannot access an invalid/free'd bitmap here!
                                A/libc: Fatal signal 6 (SIGABRT), code -1 (SI_QUEUE) in tid 8199 (ocialmediaappv2), pid 8199 (ocialmediaappv2) */

        mMap.setOnInfoWindowClickListener {
            val position = it.tag as ImageBitmap
            if (position.imageModel.picId != 0) {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("userId", position.imageModel.publisherId)
                ContextCompat.startActivity(this, intent, null)
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