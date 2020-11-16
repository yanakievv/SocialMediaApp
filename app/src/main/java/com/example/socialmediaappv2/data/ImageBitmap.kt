package com.example.socialmediaappv2.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ImageBitmap(val image: ImageModel) {
    private var bitmap: Bitmap
    private var distance: Double = 0.0

    init {
        bitmap = BitmapFactory.decodeFile(image.image)
    }

    operator fun compareTo(rhs: ImageBitmap): Int {
        return distance.compareTo(rhs.distance)
    }
    override operator fun equals(other: Any?): Boolean {
        return distance == (other as ImageBitmap).distance
    }

    override fun hashCode(): Int {
        return image.hashCode()
    }

    fun calcDistance(latLng: LatLng): Double {
        val r = 6371
        val lat1 = image.latitude
        val lat2 = latLng.latitude
        val lon1 = image.longitude
        val lon2 = latLng.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (sin(dLat / 2) * sin(dLat / 2)
                + (cos(Math.toRadians(lat1))
                * cos(Math.toRadians(lat2)) * sin(dLon / 2)
                * sin(dLon / 2)))

        distance = r * 2 * asin(sqrt(a))
        return distance
    }

    fun getDistance(): Double {
        return distance
    }

    fun updateBitmap() {
        bitmap = BitmapFactory.decodeFile(image.image)
    }
    fun getBitmap(): Bitmap {
        return bitmap
    }
}