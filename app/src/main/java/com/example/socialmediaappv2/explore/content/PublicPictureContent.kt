package com.example.socialmediaappv2.explore.content

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.socialmediaappv2.data.*
import com.example.socialmediaappv2.explore.ExploreActivity
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.*
import kotlin.system.measureTimeMillis


object PublicPictureContent {


    val SORTED_IMAGES: MutableList<ImageBitmap> = ArrayList()
    var ITEMS: MutableList<ImageModel> = ArrayList()
    var IMAGES: MutableList<ImageBitmap> = ArrayList()

    var initLoaded = false

    private lateinit var sharedPref: SharedPreference
    private lateinit var userInfo: UserInfoModel
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

        ITEMS.clear()
        IMAGES.clear()
        SORTED_IMAGES.clear()

        if (!this::userInfo.isInitialized) {
            runBlocking {userInfo = userDAO.getUser(sharedPref.getString("publisherId")!!)}
        }

        var cnt = 0
        var sem = 0

        CoroutineScope(Dispatchers.IO).launch {
            ITEMS = imageDao.getPosts(userInfo.publisherId) as MutableList<ImageModel>
            sem = ITEMS.size
            for (img in ITEMS) {
                IMAGES.add(ImageBitmap(img))
                cnt++
                Log.e("IO","Loaded image ${img.picId}, cnt = ${cnt}")
            }
            CoroutineScope(Dispatchers.Main).launch {
                Log.e("Main", "Hiding skeleton.")
                (context as ExploreActivity).hideSkeleton()
            }
            Log.e("IO", "RELEASED")
        }
        CoroutineScope(Dispatchers.Default).launch {
            while (sem == 0) {
                delay(100)
                Log.e("Default", "Waiting for items.")
            }
            var i = 0
            while (cnt <= sem) {
                if (i < cnt) {
                    IMAGES[i].calcDistance(LatLng(latLong[0], latLong[1]))
                    SORTED_IMAGES.insertAtPlace(IMAGES[i])
                    Log.e("Default", "Inserted item ${IMAGES[i].image.picId}, item number ${i}, cnt = ${cnt}, sem = ${sem}")
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

    private fun binarySearchIterative(input: MutableList<ImageBitmap>, dist: Double): Int {
        var low = 0
        var high = input.size - 1
        var mid: Int
        while (low <= high) {
            mid = low + ((high - low) / 2)
            when {
                dist > input[mid].getDistance() -> low = mid + 1
                dist == input[mid].getDistance() -> return mid
                dist < input[mid].getDistance() -> high = mid - 1
            }
        }
        return 0
    }

    private fun MutableList<ImageBitmap>.insertAtPlace(new: ImageBitmap) {
        this.add(binarySearchIterative(this, new.getDistance()), new)
    }
}