package com.example.socialmediaappv2.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.LatLng
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ImageBitmap(val imageModel: ImageModel) {
    private var thumbnail: Bitmap?
    private var bitmap: Bitmap?
    private var distance: Double = 0.0

    init {
        bitmap = BitmapFactory.decodeFile(imageModel.path)
        thumbnail = makeThumbnail(imageModel.path, 256, 144)
    }

    operator fun compareTo(rhs: ImageBitmap): Int {
        return distance.compareTo(rhs.distance)
    }
    override operator fun equals(other: Any?): Boolean {
        return distance == (other as ImageBitmap).distance
    }

    override fun hashCode(): Int {
        return imageModel.hashCode()
    }

    fun makeThumbnail(path: String, w: Int, h: Int): Bitmap {
        return BitmapFactory.Options().run {
            //inJustDecodeBounds = true
            val bmp = BitmapFactory.decodeFile(path, this)
            inSampleSize = calculateInSampleSize(this, w, h)
            //inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, this)
        }
    }

    fun calcDistance(latLng: LatLng){
        val r = 6371
        val lat1 = imageModel.latitude
        val lat2 = latLng.latitude
        val lon1 = imageModel.longitude
        val lon2 = latLng.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (sin(dLat / 2) * sin(dLat / 2)
                + (cos(Math.toRadians(lat1))
                * cos(Math.toRadians(lat2)) * sin(dLon / 2)
                * sin(dLon / 2)))

        distance = r * 2 * asin(sqrt(a))
    }

    fun getDistance(): Double {
        return distance
    }

    fun updateBitmap() {
        bitmap = BitmapFactory.decodeFile(imageModel.path)
        thumbnail = makeThumbnail(imageModel.path, 256, 144)
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }

    fun getThumbnail(): Bitmap? {
        return thumbnail
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}