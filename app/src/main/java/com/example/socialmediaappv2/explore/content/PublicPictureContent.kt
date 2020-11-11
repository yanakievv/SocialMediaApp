package com.example.socialmediaappv2.explore.content

import android.content.Context
import android.util.Log
import com.example.socialmediaappv2.data.*
import com.example.socialmediaappv2.home.content.PublisherPictureContent
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.abs
import kotlin.math.pow


object PublicPictureContent {


    var ITEMS: MutableList<ImageModel> = ArrayList()
    var initLoaded = false

    private lateinit var sharedPref: SharedPreference
    private lateinit var userInfo: UserInfoModel
    private lateinit var databaseInstance: UserDatabase
    private lateinit var userDAO: UserDAO
    private lateinit var imageDao: ImageDAO

    private var latLong: DoubleArray = doubleArrayOf(0.0, 0.0)

    fun init(radius: Double, context: Context) {
        if (!initLoaded) {
            initForce(radius, context)
            initLoaded = true
        }
    }

    fun initForce(radius: Double, context: Context) {
        Log.e("LOADFROM", "database_all_posts")

        sharedPref = SharedPreference(context)
        databaseInstance = UserDatabase.getInstance(context)
        imageDao = databaseInstance.imageDAO
        userDAO = databaseInstance.userDAO

        latLong[0] = sharedPref.getString("lat")!!.toDouble()
        latLong[1] = sharedPref.getString("long")!!.toDouble()

        runBlocking {
            userInfo = userDAO.getUser(sharedPref.getString("publisherId")!!)
            ITEMS = filterRecords(imageDao.getPosts(userInfo.publisherId) as MutableList<ImageModel>, radius)
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