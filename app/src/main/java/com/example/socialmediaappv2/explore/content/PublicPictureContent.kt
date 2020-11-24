package com.example.socialmediaappv2.explore.content

import android.content.Context
import android.util.Log
import com.example.socialmediaappv2.data.*
import com.example.socialmediaappv2.explore.ExploreActivity
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.math.*


object PublicPictureContent {


    val SORTED_IMAGES: MutableList<ImageBitmap> = ArrayList()
    var ITEMS: MutableList<ImageModel> = ArrayList()
    var IMAGES: MutableList<ImageBitmap> = ArrayList()

    var initLoaded = false

    private lateinit var sharedPref: SharedPreference
    private lateinit var databaseInstance: UserDatabase
    private lateinit var userDAO: UserDAO
    private lateinit var imageDao: ImageDAO

    private var latLong: DoubleArray = doubleArrayOf(0.0, 0.0)

    fun init(context: Context) {
        if (!initLoaded) {
            initForce(context)
            initLoaded = true
        }
    }

    fun initForce(context: Context) {
        Log.e("LOADFROM", "database_all_posts")

        CoroutineScope(Dispatchers.Main).launch {
            (context as ExploreActivity).showSkeleton()
        }

        sharedPref = SharedPreference(context)
        databaseInstance = UserDatabase.getInstance(context)
        imageDao = databaseInstance.imageDAO
        userDAO = databaseInstance.userDAO

        latLong[0] = sharedPref.getString("lat")!!.toDouble()
        latLong[1] = sharedPref.getString("long")!!.toDouble()

        IMAGES.clear()
        SORTED_IMAGES.clear()

        var cnt = 0
        var sem = 0
        var empty = false

        CoroutineScope(Dispatchers.IO).launch {
            Log.e("CURRENT_USER", sharedPref.getString("publisherId") as String)
            ITEMS = imageDao.getPosts(sharedPref.getString("publisherId") as String) as MutableList<ImageModel>
            sem = ITEMS.size
            empty = sem == 0
            for (img in ITEMS) {
                val file = File(img.path)
                if (file.exists()) {
                    IMAGES.add(ImageBitmap(img))
                    cnt++
                    Log.e("IO","Loaded image ${img.picId}, cnt = ${cnt}")
                }
                else sem--
            }
            CoroutineScope(Dispatchers.Main).launch {
                Log.e("Main", "Hiding skeleton.")
                (context as ExploreActivity).hideSkeleton()
            }
            ITEMS.clear()
            Log.e("IO", "RELEASED")
        }
        CoroutineScope(Dispatchers.Default).launch {
            while (sem == 0 && !empty) {
                delay(100)
                Log.e("Default", "Waiting for items.")
            }
            var i = 0
            while (cnt <= sem) {
                if (i < cnt) {
                    IMAGES[i].calcDistance(LatLng(latLong[0], latLong[1]))
                    SORTED_IMAGES.add(binaryInsert(SORTED_IMAGES, IMAGES[i].getDistance()), IMAGES[i])
                    Log.e("Default", "Inserted item ${IMAGES[i].imageModel.picId}, item number ${i}, cnt = ${cnt}, sem = ${sem}")
                    i++
                }
                if (i == sem) {
                    Log.e("Default", "RELEASED")
                    break
                }
            }

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

    private fun binaryInsert(input: MutableList<ImageBitmap>, dist: Double): Int {
        var low = 0
        var high = input.size - 1
        while (low < high) {
            val mid = (low + high) / 2
            if (input[mid].getDistance() < dist) low = mid + 1
            else high = mid
        }
        return low
    }

    fun nuke() {
        initLoaded = false
        IMAGES.clear()
        SORTED_IMAGES.clear()
    }
}