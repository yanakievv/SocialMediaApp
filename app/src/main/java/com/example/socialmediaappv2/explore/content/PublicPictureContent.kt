package com.example.socialmediaappv2.explore.content

import android.content.Context
import android.util.Log
import com.example.socialmediaappv2.data.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.util.*
import kotlin.math.*


object PublicPictureContent {


    var ITEMS: MutableList<ImageModel> = ArrayList()
    var ALL_ITEMS: MutableList<ImageModel> = ArrayList()
    var distances: MutableList<Double> = ArrayList()

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
            ALL_ITEMS = imageDao.getPosts(userInfo.publisherId) as MutableList<ImageModel>
            ITEMS = filterRecords(ALL_ITEMS, radius)
        }
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until ALL_ITEMS.size - 1) {
                distances.add(calcDistance(LatLng(latLong[0], latLong[1]), LatLng(ALL_ITEMS[i].latitude, ALL_ITEMS[i].longitude)))
            }
            quickSort(distances)

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

    private fun calcDistance(StartP: LatLng, EndP: LatLng): Double {
        val r = 6371
        val lat1 = StartP.latitude
        val lat2 = EndP.latitude
        val lon1 = StartP.longitude
        val lon2 = EndP.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (sin(dLat / 2) * sin(dLat / 2)
                + (cos(Math.toRadians(lat1))
                * cos(Math.toRadians(lat2)) * sin(dLon / 2)
                * sin(dLon / 2)))
        val c = 2 * asin(sqrt(a))
        val valueResult = r * c
        val km = valueResult / 1
        val newFormat = DecimalFormat("####")
        val kmInDec: Int = Integer.valueOf(newFormat.format(km))
        val meter = valueResult % 1000
        val meterInDec: Int = Integer.valueOf(newFormat.format(meter))
        Log.e(
            "DISTANCE", valueResult.toString() + "| KM: " + kmInDec
                    + "| M: " + meterInDec
        )
        return r * c
    }

    fun quickSort(array: MutableList<Double>) {
        _quickSort(array, 0, array.size - 1) //sorts the distances as well as the ALL_ITEMS by distance
    }

    private fun _quickSort(array: MutableList<Double>, left: Int, right: Int) {
        val index = partition (array, left, right)
        if(left < index-1) {
            _quickSort(array, left, index-1)
        }
        if(index < right) {
            _quickSort(array,index, right)
        }
    }

    private fun partition(array: MutableList<Double>, l: Int, r: Int): Int {
        var left = l
        var right = r
        val pivot = array[(left + right)/2]
        while (left <= right) {
            while (array[left] < pivot) left++

            while (array[right] > pivot) right--

            if (left <= right) {
                multiswap(array, left,right)
                left++
                right--
            }
        }
        return left
    }

    private fun multiswap(array: MutableList<Double>, a: Int, b: Int) {
        val temp = array[a]
        val tempImg = ALL_ITEMS[a]

        array[a] = array[b]
        array[b] = temp

        ALL_ITEMS[a] = ALL_ITEMS[b]
        ALL_ITEMS[b] = tempImg
    }

}