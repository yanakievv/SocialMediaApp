package com.example.socialmediaappv2.explore.content

import android.util.Log
import com.example.socialmediaappv2.data.App
import com.example.socialmediaappv2.data.App.currentProfile.latLong
import com.example.socialmediaappv2.data.ImageModel
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.abs
import kotlin.math.pow


object PublicPictureContent {


    var ITEMS: MutableList<ImageModel> = ArrayList()

    fun init(radius: Double) {
        Log.e("LOADFROM", "database_all_posts")
        runBlocking {
            ITEMS = filterRecords(App.imageDao.getPosts(App.currentUser.publisherId) as MutableList<ImageModel>, radius)
        }
    }

    private fun filterRecords(records: MutableList<ImageModel>, radius: Double) : MutableList<ImageModel> {
        for (i in records) {
            if (!fallsInRadius(i, latLong[0], latLong[1], radius)) {
                records.remove(i)
            }
        }
        return records
    }

    private fun fallsInRadius(image: ImageModel, lat: Double, long: Double, R: Double): Boolean {
        val dx = abs(image.longitude - long)
        val dy = abs(image.latitude - lat)

        if (dx > R || dy > R) return false
        if (dx + dy <= R) return true
        //first two checks are for optimization purposes
        return dx.pow(2) + dy.pow(2) <= R.pow(2)
    }

}