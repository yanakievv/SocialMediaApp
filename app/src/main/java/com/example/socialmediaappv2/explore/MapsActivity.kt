package com.example.socialmediaappv2.explore

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.socialmediaappv2.PreviewImageFragment
import com.example.socialmediaappv2.R
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


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var sharedPref: SharedPreference
    private lateinit var current: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        sharedPref = SharedPreference(this)
        PublicPictureContent.init(50.0, this)
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
        currentLocation.tag = ImageModel(0, "0", "0", "0", "0", 0.0, 0.0, 0)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current))

        for (i in PublicPictureContent.ALL_ITEMS) {
            val pos = LatLng(i.latitude, i.longitude)
            mMap.addMarker(
                MarkerOptions().position(LatLng(i.latitude, i.longitude))
                    .title(i.publisherDisplayName).icon(
                        BitmapDescriptorFactory.fromBitmap(
                            CircleBubbleTransformation().transform(
                                Bitmap.createScaledBitmap(
                                    BitmapFactory.decodeFile(
                                        i.image
                                    ), 320, 320, false
                                )
                            )
                        )
                    ).draggable(true)
            ).tag = i
            Log.e("IMG_ID", i.picId.toString())
        }

        mMap.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                displayFragment(marker.tag as ImageModel)
            }

            override fun onMarkerDragEnd(marker: Marker) {
                mMap.addMarker(
                    MarkerOptions().position(LatLng((marker.tag as ImageModel).latitude, (marker.tag as ImageModel).longitude))
                        .title((marker.tag as ImageModel).publisherDisplayName).icon(
                            BitmapDescriptorFactory.fromBitmap(
                                CircleBubbleTransformation().transform(
                                    Bitmap.createScaledBitmap(
                                        BitmapFactory.decodeFile(
                                            (marker.tag as ImageModel).image
                                        ), 320, 320, false
                                    )
                                )
                            )
                        ).draggable(true)
                ).tag = (marker.tag as ImageModel)
                marker.remove()
            }

            // a really hackish way to make a onMarkerLongClickListener, another way is to use onMapLongClickListener and do maths to decide which post to show,
            // but there are posts that have the same coordinates and wont work as intended

            override fun onMarkerDrag(marker: Marker) {
                // TODO Auto-generated method stub
            }
        })

        mMap.setOnInfoWindowClickListener {
            val position = it.tag as ImageModel
            if (position.picId != 0) {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("userId", position.publisherId)
                ContextCompat.startActivity(this, intent, null)
            }
        }
    }

    fun displayFragment(image: ImageModel) {
        val previewImageFragment = PreviewImageFragment(image)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(fragment_container.id, previewImageFragment).commit()

    }
}

class CircleBubbleTransformation : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val size = Math.min(source.width, source.height)
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